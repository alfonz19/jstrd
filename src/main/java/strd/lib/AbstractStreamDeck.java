package strd.lib;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import strd.lib.hid.HidLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractStreamDeck implements StreamDeck {
    private final HidLibrary.StreamDeckInfo streamDeckInfo;
    private final HidLibrary.StreamDeckHandle streamDeckHandle;

    private final int keyCount;
    //TODO MMUCHA: needed??
//    private final int rowCount;
//    private final int columnCount;

    private final List<ButtonStateListener> buttonsStateListeners = new ArrayList<>();

    public AbstractStreamDeck(HidLibrary.StreamDeckInfo streamDeckInfo,
                              HidLibrary.StreamDeckHandle streamDeckHandle,
                              int keyCount,
                              int rowCount,
                              int columnCount) {

        this.streamDeckInfo = streamDeckInfo;
        this.streamDeckHandle = streamDeckHandle;
        this.keyCount = keyCount;
//        this.rowCount = rowCount;
//        this.columnCount = columnCount;
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
    public void addDeviceRemovalListener() {
    }

    @Override
    public void setButtonImage(int buttonIndex, byte[] buttonImage) {

    }

    @Override
    public void resetDevice() {

    }

    @Override
    public void setBrightness(int percent) {

    }

    @Override
    public final void screenOff() {
        //TODO MMUCHA: store last brightness level.
        setBrightness(0);
    }

    @Override
    public void getSerialNumber() {
    }


    //TODO MMUCHA: implement.
//    public final void screenOn() {
//
//    }

    //throttling!

    @Override
    public void close() {
        streamDeckHandle.close();
    }



    
    private void registerInputReportListener() {
        streamDeckHandle.setInputReportListener(new ProcessInputReportListenerInSeparateThread());

//        Consumer<FluxSink<Tuple2<byte[], Integer>>> a = new Consumer<>() {
//
//            @Override
//            public void accept(FluxSink<Tuple2<byte[], Integer>> tuple2FluxSink) {
//                tuple2FluxSink.next()
//            }
//        };

//        Flux<Tuple2<byte[], Integer>> tuple2Flux = Flux.create(a);
//        streamDeckHandle.setInputReportListener((reportData, reportLength) -> {
//            int maxIndex = Math.min(keyCount, reportLength);
//            for(int buttonIndex = 0; buttonIndex < maxIndex; buttonIndex++) {
//                boolean oldState = buttonStates[buttonIndex];
//                boolean newState = readValueForIthButton(reportData, buttonIndex) != 0;
//
//                if (newState != oldState) {
//                    for (ButtonStateListener buttonsStateListener : buttonsStateListeners) {
//                        buttonsStateListener.buttonStateUpdated(buttonIndex, newState);
//                    }
//                }
//
//                buttonStates[buttonIndex] = newState;
//            }
//            buttonsStateListeners.forEach(e->e.buttonsStateUpdated(buttonStates));
//        });
    }

    //TODO MMUCHA: maybe this is used when not all bytes are sent in one 'packet'?
    //for some reason, indices of reported buttons are shifted. First button starts at index 3.
    private byte readValueForIthButton(byte[] reportData, Integer i) {
        return reportData[i + 3];
    }

    @Override
    public int getKeyCount() {
        return keyCount;
    }

    /**
     * This complexity is here in place for single reason: fear.
     * I can easily create {@link HidLibrary.StreamDeckHandle.InputReportListener} which will call
     * {@link #processInputReport} directly. But then I'm doing all listener logic, which might be time intensive
     * using thread related to HID, and on linux this seems to be some OS thread. I don't want to hold it too long.
     *
     * So while long and a little garbage collection 'heavy' (2 pointless objects per button press), I'm using Flux
     * to call listeners in separate thread.
     *
     * Flux is not being propagated out from this, as I don't want to make this a reactive app(because not everyone
     * needs to be familiar enough with it.)
     */
    //TODO MMUCHA: dedicated thread?
    private class ProcessInputReportListenerInSeparateThread implements HidLibrary.StreamDeckHandle.InputReportListener {
        private Consumer<Tuple2<byte[], Integer>> consumer;
        private final boolean[] buttonStates = new boolean[keyCount];


        public ProcessInputReportListenerInSeparateThread() {
            Flux.<Tuple2<byte[], Integer>>create(sink -> consumer = sink::next)
                    .publishOn(Schedulers.single())
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
                    for (ButtonStateListener buttonsStateListener : buttonsStateListeners) {
                        buttonsStateListener.buttonStateUpdated(buttonIndex, newState);
                    }
                }

                buttonStates[buttonIndex] = newState;
            }
            buttonsStateListeners.forEach(e->e.buttonsStateUpdated(buttonStates));
        }
    }
}
