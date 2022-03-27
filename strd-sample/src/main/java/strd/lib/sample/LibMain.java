package strd.lib.sample;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;
import strd.lib.common.exception.StrdException;
import strd.lib.spi.hid.HidLibrary;
import strd.lib.spi.hid.StreamDeckVariant;
import strd.lib.iconpainter.IconPainter;
import strd.lib.iconpainter.factory.IconPainterFactory;
import strd.lib.streamdeck.StreamDeck;
import strd.lib.streamdeck.StreamDeckManager;
import strd.lib.util.WaitUntilNotTerminated;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO MMUCHA: should not be part of lib.
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

    private IconPainterFactory findIconPainter(StreamDeck streamDeck) {
        return findIconPainter(streamDeck.getStreamDeckInfo().getStreamDeckVariant());
    }

    private IconPainterFactory findIconPainter(StreamDeckVariant streamDeckVariant) {
        ServiceLoader<IconPainterFactory> factories = ServiceLoader.load(IconPainterFactory.class);
        return factories.stream()
                .map(ServiceLoader.Provider::get)
                .filter(e -> e.canProcessStreamDeckVariant(streamDeckVariant))
                .findFirst()
                .orElseThrow(() -> new StrdException("Cannot find IconPainter for stream deck variant " +
                        streamDeckVariant));
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
                    if (!buttonState) {
//                        do not paint upon release
                        return;
                    }

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
                        //OOOXO
                        //OOOOO
                        //OOOOO
                        colorsWithMultiLineText(streamDeck);
                    } else if (buttonIndex == 4) {
                        //images
                        //OOOOX
                        //OOOOO
                        //OOOOO
                        someImages2(streamDeck);
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
                    } else if (buttonIndex == 12) {
                        //some images3
                        //OOOOO
                        //OOOOO
                        //OOXOO
                        someImages3(streamDeck);
                    } else if (buttonIndex == 13) {
                        //RESET
                        //OOOOO
                        //OOOOO
                        //OOOXO
                        streamDeck.resetDevice();

                    } else if (buttonIndex == 14) {
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
        IconPainterFactory iconPainter = findIconPainter(streamDeck);

        IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount()).forEach(index -> {
            Color color = COLORS_FOR_BUTTONS.get(index % COLORS_FOR_BUTTONS.size());
            byte[] buttonImage = iconPainter.create(streamDeck)
                    .fillWholeIcon(color.getRed(), color.getGreen(), color.getBlue())
                    .toDeviceNativeFormat();
            streamDeck.setButtonImage((byte)index, buttonImage);
        });
    }

    public void colorsWithSingleLineText(StreamDeck streamDeck) {
        IconPainterFactory iconPainter = findIconPainter(streamDeck);

        IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount()).forEach(index -> {
            Color color = COLORS_FOR_BUTTONS.get(index % COLORS_FOR_BUTTONS.size());

            char c = (char) ('A' + (byte) index);
            char[] chars = new char[(byte) index + 1];
            Arrays.fill(chars, c);
            String buttonText = new String(chars);

            byte[] buttonImage = iconPainter.create(streamDeck)
                    .fillWholeIcon(color.getRed(), color.getGreen(), color.getBlue())
                    .setColor(0, 0, 0)
                    .setFont(null, 16, IconPainter.FontStyle.BOLD_ITALIC)
                    .writeTextCentered(buttonText)
                    .toDeviceNativeFormat();
            streamDeck.setButtonImage((byte) index, buttonImage);

        });
    }

    public void colorsWithMultiLineText(StreamDeck streamDeck) {
        IconPainterFactory iconPainter = findIconPainter(streamDeck);

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

            byte[] buttonImage = iconPainter.create(streamDeck)
                    .fillWholeIcon(color.getRed(), color.getGreen(), color.getBlue())
                    .setColor(0, 0, 0)
                    .writeTextCentered(multilineText)
                    .toDeviceNativeFormat();
            streamDeck.setButtonImage((byte) index, buttonImage);

        });
    }

    public void someImages(StreamDeck streamDeck) {
        IconPainterFactory iconPainterFactory = findIconPainter(streamDeck);
        byte[] bytes = readPhotoFromFile("/magda.jpg");

        AtomicLong totalImagePreparationTime = new AtomicLong(0);
        AtomicLong totalSettingTime = new AtomicLong(0);

        IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount()).forEach(index -> {
            long start = System.nanoTime();
            byte[] buttonImage = iconPainterFactory.create(streamDeck, bytes).toDeviceNativeFormat();
            long imageCreationTime = System.nanoTime();
            streamDeck.setButtonImage((byte)index, buttonImage);
            long end = System.nanoTime();

            long imagePreparationTime = TimeUnit.NANOSECONDS.toMillis(imageCreationTime - start);
            long settingTime = TimeUnit.NANOSECONDS.toMicros(end - imageCreationTime);
            log.debug("Total button painting time: {}ms, image={}ms, setting={}us", TimeUnit.NANOSECONDS.toMillis(end-start),
                    imagePreparationTime,
                    settingTime);

            totalImagePreparationTime.addAndGet(imagePreparationTime);
            totalSettingTime.addAndGet(settingTime);
        });

        log.info("All buttons painting time: {}ms, image={}ms, setting={}ms", totalImagePreparationTime.get()+totalSettingTime.get()/1000,
                totalImagePreparationTime.get(),
                totalSettingTime.get()/1000);

    }

    public void someImages2(StreamDeck streamDeck) {
        IconPainterFactory iconPainterFactory = findIconPainter(streamDeck);
        byte[] imageBytes = readPhotoFromFile("/magda.jpg");
        byte[] bytes = iconPainterFactory.create(streamDeck, imageBytes).toDeviceNativeFormat();

        Map<Integer, List<byte[]>> buttonBytesForEachButton =
                IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount())
                        .boxed()
                        .collect(Collectors.toMap(Function.identity(),
                                index -> streamDeck.splitNativeImageBytes(index, bytes)));


        long start = System.nanoTime();
        buttonBytesForEachButton.values().forEach(streamDeck::setButtonImage);
        long end = System.nanoTime();

        log.info("All buttons painted in {}us", TimeUnit.NANOSECONDS.toMicros(end-start));

    }

    //hypothetical speedup
    public void someImages3(StreamDeck streamDeck) {
        IconPainterFactory iconPainterFactory = findIconPainter(streamDeck);
        byte[] imageBytes = readPhotoFromFile("/magda.jpg");
        byte[] bytes = iconPainterFactory.create(streamDeck, imageBytes).toDeviceNativeFormat();

        Map<Integer, List<byte[]>> buttonBytesForEachButton =
                IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount())
                        .boxed()
                        .collect(Collectors.toMap(Function.identity(),
                                index -> streamDeck.splitNativeImageBytes(index, bytes)));


        long start = System.nanoTime();
        buttonBytesForEachButton.values().forEach(streamDeck::setButtonImage);
        long end = System.nanoTime();

        log.info("All buttons painted in {}us", TimeUnit.NANOSECONDS.toMicros(end-start));

    }

    public void someImagesParallel(StreamDeck streamDeck) {
        IconPainterFactory iconPainterFactory = findIconPainter(streamDeck);
        byte[] bytes = readPhotoFromFile("/magda.jpg");

        long start = System.nanoTime();
        Flux.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount())
                .publishOn(Schedulers.newParallel("img", 15, true))
                .doOnNext(e->log.debug("calculating on thread {}", Thread.currentThread().getName()))
                .map(index -> {
                    byte[] bytes1 = iconPainterFactory.create(streamDeck, bytes).toDeviceNativeFormat();
                    return Tuples.of(index, bytes1);
                })
                .publishOn(Schedulers.single())
                .doOnNext(e->log.debug("setting on thread {}", Thread.currentThread().getName()))
                .subscribe(tuple -> streamDeck.setButtonImage(tuple.getT1(), tuple.getT2()), e -> {}, () -> {
                    long end = System.nanoTime();
                    log.info("Total time: {}ms", TimeUnit.NANOSECONDS.toMillis(end-start));
                });
    }

    private byte[] readPhotoFromFile(String name) {
        try (InputStream resourceAsStream = LibMain.class.getResourceAsStream(name)) {
            if (resourceAsStream == null) {
                return null;
            }

            try (BufferedInputStream bis = new BufferedInputStream(resourceAsStream);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                bos.write(bis.readAllBytes());
                return bos.toByteArray();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void colorsWithSomeGraphics(StreamDeck streamDeck) {
        IconPainterFactory iconPainter = findIconPainter(streamDeck);

        IntStream.range(0, streamDeck.getStreamDeckInfo().getStreamDeckVariant().getKeyCount()).forEach(index -> {
            Color color = COLORS_FOR_BUTTONS.get(index % COLORS_FOR_BUTTONS.size());

            int iconSize = streamDeck.getStreamDeckInfo().getStreamDeckVariant().getPixelCountPerIconSide();
            int xy1 = iconSize / 3;
            int xy2 = xy1 * 2;


            byte[] buttonImage = iconPainter.create(streamDeck)
                    .fillWholeIcon(color.getRed(), color.getGreen(), color.getBlue())
                    .setColor(255, 255, 255)
                    .fillRect(xy1, xy1, xy2, xy2)
                    .toDeviceNativeFormat();
            streamDeck.setButtonImage((byte) index, buttonImage);

        });
    }
}
