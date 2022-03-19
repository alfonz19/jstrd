package strd.lib.example;

import strd.lib.streamdeck.IconPainterFactory;
import strd.lib.streamdeck.StreamDeck;
import strd.lib.streamdeck.StreamDeckManager;
import strd.lib.util.WaitUntilNotTerminated;
import strd.lib.hid.HidLibrary;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibMain {
    private static final Logger log = LoggerFactory.getLogger(LibMain.class);

    public static void main(String[] args) {
        new LibMain().run();
    }

    private void run() {
        ServiceLoader<HidLibrary> load = ServiceLoader.load(HidLibrary.class);
        StreamSupport.stream(load.spliterator(), false)
                .findFirst()
                .ifPresentOrElse(this::run, errorMessage("No HID library found, Unable to proceed."));
    }

    private void run(HidLibrary hidLibrary) {
        StreamDeckManager streamDeckManager = new StreamDeckManager(hidLibrary);
        List<HidLibrary.StreamDeckInfo> streamDeckDevices = streamDeckManager.findStreamDeckDevices();


        String foundStreamDeckDevicesString = streamDeckDevices.stream()
                .map(HidLibrary.StreamDeckInfo::toString)
                .collect(Collectors.joining("\n",
                        "-----All found streamdeck devices-----\n",
                        "--------------------------------------\n"));
        System.out.println(foundStreamDeckDevicesString);
        System.out.println("Ready to go.");


        streamDeckDevices.stream()
                .filter(e -> e.getSerialNumberString().equals("DL49K1A69132"))
                .findFirst()
                .ifPresentOrElse(streamDeckInfo -> testRunMyStreamDeck(streamDeckManager, streamDeckInfo),
                        errorMessage("Unable to find selected streamdeck."));
    }

    private void testRunMyStreamDeck(StreamDeckManager streamDeckManager, HidLibrary.StreamDeckInfo streamDeckInfo) {
        WaitUntilNotTerminated waitUntilNotTerminated = new WaitUntilNotTerminated(250);

        try (StreamDeck streamDeck = streamDeckManager.openConnection(streamDeckInfo)) {
            streamDeck.addButtonsStateUpdatedListener(new StreamDeck.ButtonStateListener.Adapter() {
                @Override
                public void buttonStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo,
                                               int buttonIndex,
                                               boolean buttonState) {
                    log.info("Button {} {}", buttonIndex, buttonState ? "pressed" : "released");

//                    END
//                    OOOOO
//                    OOOOO
//                    OOOOX
                    int keyCount = streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount();
                    if (buttonIndex == keyCount - 1 && buttonState) {
                        waitUntilNotTerminated.terminate();

//                    RESET
//                    OOOOO
//                    OOOOO
//                    OOOXO
                    } else if (buttonIndex == keyCount - 2 && buttonState) {
                        streamDeck.resetDevice();

//                    BRIGHTNESS
//                    OOOOO
//                    XXXXX
//                    OOOOO
                    } else if (buttonIndex >= 5 && buttonIndex <= 9) {
                        streamDeck.setBrightness((buttonIndex - 4)*20);

//                    SCREEN OFF
//                    OOOOO
//                    OOOOO
//                    XOOOO
                    } else if (buttonIndex == 10) {
                        streamDeck.screenOff();

//                    SCREEN ON
//                    OOOOO
//                    OOOOO
//                    OXOOO
                    } else if (buttonIndex == 11) {
                        streamDeck.screenOn();
//                    COLOR
//                    OOOOO
//                    OOOOO
//                    OOXOO
                    } else if (buttonIndex == 12) {
                        color(streamDeck);
                    }
                }
            });

//            System.out.println("StreamDeck serial version(A): "+streamDeck.getStreamDeckInfo().getSerialNumberString());
//            System.out.println("StreamDeck serial version(B): "+streamDeck.getSerialNumber());

            waitUntilNotTerminated.start();
        }
    }

    private Runnable errorMessage(String message) {
        return () -> System.err.println(message);
    }

    public void color(StreamDeck streamDeck) {
        IconPainterFactory iconPainter = IconPainterFactory.findIconPainter(streamDeck);

        List<Color> colors = Arrays.asList(Color.RED,
                Color.GREEN,
                Color.ORANGE,
                Color.GRAY,
                Color.WHITE,
                Color.BLUE,
                Color.CYAN,
                Color.MAGENTA);

        IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount()).forEach(index -> {
            Color color = colors.get(index % colors.size());
            color((byte)index, color, iconPainter, streamDeck);

        });
    }

    private void color(byte buttonIndex,
                       Color color,
                       IconPainterFactory iconPainter,
                       StreamDeck streamDeck) {

        byte[] buttonImage = iconPainter.create(color.getRed(), color.getGreen(), color.getBlue()).toDeviceNativeFormat();
        streamDeck.setButtonImage(buttonIndex, buttonImage);
    }

    private static class LoopingIndex implements Supplier<Integer> {
        private final int arraySize;
        int i;

        public LoopingIndex(int arraySize) {
            this.arraySize = arraySize;
            i = 0;
        }

        @Override
        public Integer get() {
            if (i < arraySize) {
                return i++;
            } else {
                i=0;
                return i++;
            }
        }
    }
}
