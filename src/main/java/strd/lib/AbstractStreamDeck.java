package strd.lib;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.SynchronousSink;
import reactor.util.function.Tuple2;
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
    private final boolean[] buttonStates;

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
        buttonStates = new boolean[this.keyCount];
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
}
