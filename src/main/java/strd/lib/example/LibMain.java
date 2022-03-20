package strd.lib.example;

import strd.lib.hid.HidLibrary;
import strd.lib.streamdeck.IconPainterFactory;
import strd.lib.streamdeck.StreamDeck;
import strd.lib.streamdeck.StreamDeckManager;
import strd.lib.util.WaitUntilNotTerminated;

import javax.sound.midi.Soundbank;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibMain {
    public static final List<Color> COLORS_FOR_BUTTONS = Arrays.asList(Color.RED,
            Color.GREEN,
            Color.ORANGE,
            Color.GRAY,
            Color.WHITE,
            Color.BLUE,
            Color.CYAN,
            Color.MAGENTA);
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

                    int keyCount = streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount();

                    if (buttonIndex == 0) {
                        //COLORS ONLY
                        //XOOOO
                        //OOOOO
                        //OOOOO
                        colorsOnly(streamDeck);
                    } else if (buttonIndex == 1) {
                        //COLORS AND TEXT
                        //OXOOO
                        //OOOOO
                        //OOOOO
                        colorsWithSingleLineText(streamDeck);
                    } else if (buttonIndex == 2) {
                        //COLORS AND BASIC GRAPHICS
                        //OOXOO
                        //OOOOO
                        //OOOOO
                        colorsWithSomeGraphics(streamDeck);
                    } else if (buttonIndex == 3) {
                        //COLORS AND MULTILINE TEXT
                        //OOXOO
                        //OOOOO
                        //OOOOO
                        colorsWithMultiLineText(streamDeck);
                    } else if (buttonIndex >= 5 && buttonIndex <= 9) {
                        //BRIGHTNESS
                        //OOOOO
                        //XXXXX
                        //OOOOO
                        streamDeck.setBrightness((buttonIndex - 4) * 20);

                    } else if (buttonIndex == 10) {
                        //SCREEN OFF
                        //OOOOO
                        //OOOOO
                        //XOOOO
                        streamDeck.screenOff();

                    } else if (buttonIndex == 11) {
                        //SCREEN ON
                        //OOOOO
                        //OOOOO
                        //OXOOO
                        streamDeck.screenOn();
                    } else if (buttonIndex == keyCount - 2 && buttonState) {
                        //RESET
                        //OOOOO
                        //OOOOO
                        //OOOXO
                        streamDeck.resetDevice();

                    } else if (buttonIndex == keyCount - 1 && buttonState) {
                        //END
                        //OOOOO
                        //OOOOO
                        //OOOOX
                        waitUntilNotTerminated.terminate();
                    }

                }});

//            System.out.println("StreamDeck serial version(A): "+streamDeck.getStreamDeckInfo().getSerialNumberString());
//            System.out.println("StreamDeck serial version(B): "+streamDeck.getSerialNumber());

            waitUntilNotTerminated.start();
        }
    }

    private Runnable errorMessage(String message) {
        return () -> System.err.println(message);
    }

    public void colorsOnly(StreamDeck streamDeck) {
        IconPainterFactory iconPainter = IconPainterFactory.findIconPainter(streamDeck);

        IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount()).forEach(index -> {
            Color color = COLORS_FOR_BUTTONS.get(index % COLORS_FOR_BUTTONS.size());
            byte[] buttonImage = iconPainter.create(color.getRed(), color.getGreen(), color.getBlue()).toDeviceNativeFormat();
            streamDeck.setButtonImage((byte)index, buttonImage);
        });
    }

    public void colorsWithSingleLineText(StreamDeck streamDeck) {
        IconPainterFactory iconPainter = IconPainterFactory.findIconPainter(streamDeck);

        IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount()).forEach(index -> {
            Color color = COLORS_FOR_BUTTONS.get(index % COLORS_FOR_BUTTONS.size());

            char c = (char) ('A' + (byte) index);
            char[] chars = new char[(byte) index + 1];
            Arrays.fill(chars, c);
            String buttonText = new String(chars);

            byte[] buttonImage = iconPainter.create(color.getRed(), color.getGreen(), color.getBlue())
                    .setColor(0, 0, 0)
                    .writeTextCentered(buttonText)
                    .toDeviceNativeFormat();
            streamDeck.setButtonImage((byte) index, buttonImage);

        });
    }

    public void colorsWithMultiLineText(StreamDeck streamDeck) {
        IconPainterFactory iconPainter = IconPainterFactory.findIconPainter(streamDeck);

        IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount()).forEach(index -> {
            Color color = COLORS_FOR_BUTTONS.get(index % COLORS_FOR_BUTTONS.size());

            char c = (char) ('A' + (byte) index);
            int maxLines = 5;
            String multilineText = IntStream.rangeClosed(0, index % maxLines)
                    .boxed()
                    .map(ii->IntStream.rangeClosed(0, ii % maxLines)
                            .boxed()
                            .map(f->Character.valueOf(c).toString())
                            .collect(Collectors.joining()))
                    .collect(Collectors.joining("\n"));

            byte[] buttonImage = iconPainter.create(color.getRed(), color.getGreen(), color.getBlue())
                    .setColor(0, 0, 0)
                    .writeTextCentered(multilineText)
                    .toDeviceNativeFormat();
            streamDeck.setButtonImage((byte) index, buttonImage);

        });
    }

    public void colorsWithSomeGraphics(StreamDeck streamDeck) {
        IconPainterFactory iconPainter = IconPainterFactory.findIconPainter(streamDeck);

        IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount()).forEach(index -> {
            Color color = COLORS_FOR_BUTTONS.get(index % COLORS_FOR_BUTTONS.size());

            int iconSize = streamDeck.getStreamDeckInfo().getStreamDeckVariant().getPixelCountPerIconSide();
            int xy1 = iconSize / 3;
            int xy2 = xy1 * 2;


            byte[] buttonImage = iconPainter.create(color.getRed(), color.getGreen(), color.getBlue())
                    .setColor(255, 255, 255)
                    .fillRect(xy1, xy1, xy2, xy2)
                    .toDeviceNativeFormat();
            streamDeck.setButtonImage((byte) index, buttonImage);

        });
    }
}
