package strd.lib.example;

import strd.lib.streamdeck.StreamDeck;
import strd.lib.streamdeck.StreamDeckManager;
import strd.lib.util.WaitUntilNotTerminated;
import strd.lib.hid.HidLibrary;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
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
        color((byte)0, Color.RED, streamDeck);
        color((byte)1, Color.GREEN, streamDeck);
        color((byte)2, Color.ORANGE, streamDeck);
        color((byte)3, Color.GRAY, streamDeck);
        color((byte)4, Color.WHITE, streamDeck);
        color((byte)5, Color.BLUE, streamDeck);
        color((byte)6, Color.CYAN, streamDeck);
        color((byte)7, Color.MAGENTA, streamDeck);

        color((byte)8, Color.RED, streamDeck);
        color((byte)9, Color.GREEN, streamDeck);
        color((byte)10, Color.ORANGE, streamDeck);
        color((byte)11, Color.GRAY, streamDeck);
        color((byte)12, Color.WHITE, streamDeck);
        color((byte)13, Color.BLUE, streamDeck);
        color((byte)14, Color.CYAN, streamDeck);
    }

    private void color(byte buttonIndex, Color color, StreamDeck streamDeck) {
        byte[] buttonImage = createColoredIcon(color);
        streamDeck.setButtonImage(buttonIndex, buttonImage);
    }

    public static final int ICON_SIZE = 72;
    private static byte[] createColoredIcon(Color color) {
        BufferedImage off_Image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = off_Image.createGraphics();
        g2.setColor(color);
        g2.fillRect(0, 0, ICON_SIZE, ICON_SIZE);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(off_Image, "jpg", bos);
            g2.dispose();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("write failed");
        }
    }
}
