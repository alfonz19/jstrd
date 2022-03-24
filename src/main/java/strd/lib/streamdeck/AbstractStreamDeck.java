package strd.lib.streamdeck;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import strd.lib.hid.HidLibrary.StreamDeckInfo;
import strd.lib.hid.StreamDeckHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractStreamDeck implements StreamDeck {
    protected final StreamDeckHandle streamDeckHandle;


    private final List<ButtonStateListener> buttonsStateListeners = new ArrayList<>();
    //TODO MMUCHA: externalize
    private int lastSetScreenBrightness = 10;
    private final StreamDeckInfo streamDeckInfo;

    public AbstractStreamDeck(StreamDeckHandle streamDeckHandle) {

        this.streamDeckHandle = streamDeckHandle;
        this.streamDeckInfo = streamDeckHandle.getStreamDeckInfo();
        registerInputReportListener();
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
        setBrightnessImpl(percent);
    }

    protected abstract void setBrightnessImpl(int percent);

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
}
