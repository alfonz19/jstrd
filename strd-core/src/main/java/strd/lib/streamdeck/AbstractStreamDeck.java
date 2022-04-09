package strd.lib.streamdeck;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import strd.lib.common.Constants;
import strd.lib.spi.hid.HidLibrary.StreamDeckInfo;
import strd.lib.spi.hid.StreamDeckHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractStreamDeck implements StreamDeckDevice {
    private static final Logger log = getLogger(AbstractStreamDeck.class);

    private final StreamDeckHandle streamDeckHandle;

    private final List<ButtonStateListener> buttonsStateListeners = new ArrayList<>();
    private final StreamDeckCommandsProcessor streamDeckCommandsProcessor;
    private int lastSetScreenBrightness = Constants.INITIAL_LAST_SET_SCREEN_BRIGHTNESS;
    private final StreamDeckInfo streamDeckInfo;

    public AbstractStreamDeck(StreamDeckHandle streamDeckHandle) {

        this.streamDeckHandle = streamDeckHandle;
        this.streamDeckInfo = streamDeckHandle.getStreamDeckInfo();
        registerInputReportListener();
        streamDeckCommandsProcessor = new StreamDeckCommandsProcessor(streamDeckHandle);
    }

    @Override
    public void addButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener) {
        buttonsStateListeners.add(buttonsStateUpdatedListener);
    }

    @Override
    public void removeButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener) {
        buttonsStateListeners.remove(buttonsStateUpdatedListener);
    }

    @Override
    public void removeAllButtonsStateUpdatedListeners() {
        buttonsStateListeners.clear();
    }

    @Override
    public void setDeviceRemovalListener(DeviceRemovalListener deviceRemovalListener) {
        streamDeckHandle.setDeviceRemovalListener(deviceRemovalListener::deviceRemoved);
    }

    @Override
    public final void setBrightness(int percent) {
        lastSetScreenBrightness = percent != 0 ? percent : lastSetScreenBrightness;
        streamDeckCommandsProcessor.addCommand(createSetBrightnessCommand(percent));
    }

    @Override
    public final void resetDevice() {
        streamDeckCommandsProcessor.addCommand(createResetCommand());
    }

    protected abstract StreamDeckCommand createResetCommand();

    @Override
    public final void setButtonImage(List<byte[]> payloadsBytes) {
        streamDeckCommandsProcessor.addCommand(createSetButtonImageCommand(payloadsBytes));
    }

    protected abstract SetButtonImageCommand createSetButtonImageCommand(List<byte[]> payloadsBytes);


    protected abstract SetBrightnessCommand createSetBrightnessCommand(int percent);

    @Override
    public final void screenOff() {
        setBrightness(0);
    }

    @Override
    public final void screenOn() {
        setBrightness(lastSetScreenBrightness);
    }

    @Override
    public boolean isClosed() {
        return streamDeckHandle.isClosed();
    }

    @Override
    public void close() {
        streamDeckHandle.close();
    }

    private void registerInputReportListener() {
        streamDeckHandle.setInputReportListener(new ProcessInputReportListenerInSeparateThread(this));
    }

    @Override
    public StreamDeckInfo getStreamDeckInfo() {
        return streamDeckInfo;
    }

    /**
     * This complexity is here in place for 2 reasons: a) this whole library is NOT thread safe. b) fear.
     *
     * ad A: so if we have multiple events in quick succession there might be concurrency issues. Serializing incoming
     * events into single thread is easier/more performant than locking.
     *
     * ad B: I can see java thread creation in HID library source,
     * but the thread name is really suspicious and I don't see that name assigned anywhere. Better be safe than sorry,
     * I will assume, that this is called from some non-java thread, some operating system thread. Probably incorrect,
     * yet fixing it is easy. So...
     *
     * I can easily create {@link StreamDeckHandle.InputReportListener} which will call
     * {@link #processInputReport} directly. But then I'm doing all listener logic, which might be time intensive
     * using thread related to HID, and on linux this seems to be some OS thread. I don't want to hold it too long.
     *
     * So while long and a little garbage collection 'heavy' (2 pointless objects per button press), I'm using Flux
     * to call listeners in separate thread.
     *
     * Flux is not being propagated out from this, as I don't want to make this a reactive app(because not everyone
     * needs to be familiar enough with it.)
     */
    private static class ProcessInputReportListenerInSeparateThread implements StreamDeckHandle.InputReportListener {
        private static final Scheduler scheduler = Schedulers.newSingle("button-listener", true);

        private final int keyCount;
        private final StreamDeckInfo streamDeckInfo;
        private final AbstractStreamDeck streamDeck;
        private Consumer<Tuple2<byte[], Integer>> consumer;
        private final boolean[] buttonStates;


        public ProcessInputReportListenerInSeparateThread(AbstractStreamDeck streamDeck) {
            this.streamDeck = streamDeck;
            streamDeckInfo = streamDeck.getStreamDeckInfo();
            this.keyCount = streamDeckInfo.getStreamDeckVariant().getKeyCount();
            buttonStates = new boolean[keyCount];

            Flux.<Tuple2<byte[], Integer>>create(sink -> consumer = sink::next)
                    .publishOn(scheduler)
                    .subscribe(this::processInputReport);
        }

        @Override
        public void onInputReport(byte[] reportData, int reportLength) {
            consumer.accept(Tuples.of(reportData, reportLength));
        }

        private void processInputReport(Tuple2<byte[], Integer> tuple2) {
            byte[] reportData = tuple2.getT1();
            int reportLength = tuple2.getT2();

            int maxIndex = Math.min(keyCount, reportLength);
            for(int buttonIndex = 0; buttonIndex < maxIndex; buttonIndex++) {
                boolean oldState = buttonStates[buttonIndex];
                boolean newState = readValueForIthButton(reportData, buttonIndex) != 0;

                if (newState != oldState) {
                    for (ButtonStateListener buttonsStateListener : streamDeck.buttonsStateListeners) {
                        buttonsStateListener.buttonStateUpdated(streamDeckInfo, buttonIndex, newState);
                    }
                }

                buttonStates[buttonIndex] = newState;
            }
            streamDeck.buttonsStateListeners.forEach(e->e.buttonsStateUpdated(streamDeckInfo, buttonStates));
        }

        //Maybe this is used when not all bytes are sent in one 'packet'?
        //
        //for some reason, indices of reported buttons are shifted. First button starts at index 3.
        private byte readValueForIthButton(byte[] reportData, Integer i) {
            return reportData[i + 3];
        }
    }

    private static class StreamDeckCommandsProcessor {
        private static final Scheduler scheduler = Schedulers.newSingle("command-processor", true);
        private Consumer<StreamDeckCommand> consumer;

        public StreamDeckCommandsProcessor(StreamDeckHandle streamDeckHandle) {
            Disposable commandFlux = Flux.<StreamDeckCommand>create(sink -> consumer = sink::next)
                    .publishOn(scheduler)
                    .doOnNext(e -> e.processCommand(streamDeckHandle))
                    .subscribe();
        }

        public void addCommand(StreamDeckCommand setOutputReportCommand) {
            consumer.accept(setOutputReportCommand);
        }
    }

    protected interface StreamDeckCommand {
        void processCommand(StreamDeckHandle streamDeckHandle);
    }

    protected static class SetButtonImageCommand implements StreamDeckCommand {

        private final List<byte[]> payloadsBytesList;
        private final int reportId;

        public SetButtonImageCommand(int reportId, List<byte[]> payloadsBytesList) {
            this.reportId = reportId;
            this.payloadsBytesList = payloadsBytesList;
        }

        @Override
        public void processCommand(StreamDeckHandle streamDeckHandle) {
            payloadsBytesList.forEach(e -> {
                int result = streamDeckHandle.setOutputReport((byte) reportId, e, e.length);
                if (result == -1) {
                    log.error("Setting outputReport failed");
                }
            });
        }
    }

    protected static class SetBrightnessCommand implements StreamDeckCommand {

        private final int reportId;
        private final byte[] payload;
        private final int length;

        public SetBrightnessCommand(int reportId, byte[] payload, int length) {

            this.reportId = reportId;
            this.payload = payload;
            this.length = length;
        }

        @Override
        public void processCommand(StreamDeckHandle streamDeckHandle) {
            int result = streamDeckHandle.setFeatureReport((byte)reportId, payload, length);
            if (result == -1) {
                log.error("Setting feature report failed");
            }
        }
    }

    protected static class ResetCommand implements StreamDeckCommand {

        private final byte reportId;
        private final byte[] payload;
        private final int length;

        public ResetCommand(byte reportId, byte[] payload, int length) {

            this.reportId = reportId;
            this.payload = payload;
            this.length = length;
        }

        @Override
        public void processCommand(StreamDeckHandle streamDeckHandle) {
            int result = streamDeckHandle.setFeatureReport(reportId, payload, length);
            if (result == -1) {
                log.error("Setting feature report failed");
            }
        }
    }
}
