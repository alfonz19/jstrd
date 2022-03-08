package alf;

import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;
import purejavahidapi.PureJavaHidApi;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static final int VENDOR_ID = 4057;
    public static final int PRODUCT_ID = 128;
    private HidDevice hidDevice = null;
    private boolean terminate = false;


    public static void main(String[] args) {
        new Main().test();
    }

    private void test() {
        HidDeviceInfo info = findStreamDeckDevice_noLog();
//        printInfoAboutDevice(info);

        ShutdownHooks.register(() -> {
            terminate = true;
            System.out.println("terminating.");
            if (hidDevice == null) {
                System.out.println("hid device not opened, not attempting to close.");
            } else {
                try {
                    System.out.println("attempting to close device");
                    hidDevice.close();
                    System.out.println("hid to closed");
                } catch (Exception e) {
                    System.err.println("error closing hidDevice");
                    e.printStackTrace(System.err);
                }
            }
        });

        //--------------
        try {
            log.info("opening device");
            hidDevice = PureJavaHidApi.openDevice(info);
            log.info("device opened.");
            hidDevice.setInputReportListener(new InputReportListener() {
                @Override
                public void onInputReport(HidDevice source, byte reportID, byte[] reportData, int reportLength) {
//                    if (reportID == 1) {
//                        for (int i = 0; i < 15 && i < reportLength; i++) {
//                            boolean bool = reportData[i] == 0x01;
//                            if (keysPressed[i] != bool) {
//                                fireKeyChangedEvent(i, bool);
//                                keysPressed[i] = bool;
//                            }
//                        }
//                    }
//                    log.info(String.format("%nreportID=%d%n%s%n---------------", reportID, IntStream.range(0, Math.min(15, reportLength))
//                            .boxed()
//                            .map(i->String.format("button %d = %b%n", i, reportData[i]))
//                            .collect(Collectors.joining())));

//                    String arrDesc = IntStream.range(0, reportLength)
//                            .boxed()
//                            .map(i -> String.format("i=%d: %s%n", i, reportData[i]))
//                            .collect(Collectors.joining());
//                    log.info(String.format("reportLength=%d, reportID=%d:%n%s%n-----------", reportLength, reportID, arrDesc));

                    //TODO MMUCHA: reportID -- no idea what that is actually. It's some sort of 'report number' produced only by linux read, which may and may not be present.
                    //some ignores events with reportID other than 1, while pureJavaHID uses 0 if numbered reports are not used.
                    //and if they are used, the report id is sent as first value in array (from which is it then removed)
                    //so I don't understand why 1 should be the correct value. Wild guess: guy implementing that app
                    //which filters on value 1 just has numbered reports ON on his machine, and his os for some reason
                    //always generates 1.

                    String pressedButtons = IntStream.range(0, Math.min(15, reportLength))
                            .boxed()
                            .filter(i-> readValueForIthButton(reportData, i) != 0)
                            .map(i -> String.format("%d(%d)", i, readValueForIthButton(reportData, i)))
                            .collect(Collectors.joining(", ", "Pressed buttons: ", ""));

                    log.info(String.format("reportLength=%d, reportID=%d. %s", reportLength, reportID, (pressedButtons.isEmpty() ? "no pressed buttons" : pressedButtons)));

                }

                //for some reason, indices of reported buttons are shifted. First button starts at index 3.
                //So if we want to read buttons starting with index 0, we need to look a little bit further into array.
                private byte readValueForIthButton(byte[] reportData, Integer i) {
                    return reportData[i + 3];
                }
            });

            hidDevice.setDeviceRemovalListener(new DeviceRemovalListener() {
                @Override
                public void onDeviceRemoval(HidDevice source) {
                    log.info("device removed");
                    hidDevice = null;
                    System.exit(0);
                }
            });

            System.out.println(Arrays.stream(Main.class.getDeclaredMethods()).filter(e->Modifier.isPublic(e.getModifiers())).filter(e->e.getParameterTypes().length ==0).map(Method::getName).collect(Collectors.joining("\n", "existing public no-arg methods: \n", "")));
            System.out.println("Submit commands(quit): ");
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.isEmpty()) continue;
                if (line.equals("quit")) { break; }

                try {
                    Method method = Main.class.getMethod(line);
                    method.invoke(this);
                } catch (NoSuchMethodException e) {
                    System.err.println("unable to invoke method: "+line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            riColor1();
        } catch (IOException e) {
            throw new RuntimeException("Unable to open", e);
        }
    }

    private void printInfoAboutDevice(HidDeviceInfo info) {
        log.info("using streamdeck:");
        log.info("  Manufacurer: " + info.getManufacturerString());
        log.info("  Product:     " + info.getProductString());
        log.info("  Device-Id:   " + info.getDeviceId());
        log.info("  Serial-No:   " + info.getSerialNumberString());
        log.info("  Path:        " + info.getPath());
    }

    private HidDeviceInfo findStreamDeckDevice() {

        List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
        log.info("number of devices: {}", devList.size());
        for (HidDeviceInfo info : devList) {
            log.debug("Vendor-ID: " + Integer.toHexString((int)info.getVendorId()) + ", Product-ID: " + Integer.toHexString((int)info.getProductId()));
            if (info.getVendorId() == VENDOR_ID && info.getProductId() == PRODUCT_ID) {
                log.info("Found ESD ["+info.getVendorId()+":"+info.getProductId()+"]");
                return info;
            }
        }

        log.error("Streamdeck not found");
        System.exit(1);
        return null;
    }

    private HidDeviceInfo findStreamDeckDevice_noLog() {

        List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
        for (HidDeviceInfo info : devList) {
            if (info.getVendorId() == VENDOR_ID && info.getProductId() == PRODUCT_ID) {
                return info;
            }
        }

        log.error("Streamdeck not found");
        System.exit(1);
        return null;
    }

    //----------------------------------------------

    private static final byte[] RESET_DATA = new byte[] { 0x0B, 0x63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    /**
     * Header for Page 1 of the image command
     */
    private static final byte[] PAGE_1_HEADER = new byte[] { 0x01, 0x01, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x42, 0x4D, (byte) 0xF6, 0x3C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x36, 0x00, 0x00,
            0x00, 0x28, 0x00, 0x00, 0x00, 0x48, 0x00, 0x00, 0x00, 0x48, 0x00, 0x00, 0x00, 0x01, 0x00, 0x18, 0x00, 0x00,
            0x00, 0x00, 0x00, (byte) 0xC0, 0x3C, 0x00, 0x00, (byte) 0xC4, 0x0E, 0x00, 0x00, (byte) 0xC4, 0x0E, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    /**
     * Header for Page 2 of the image command
     */
    private static final byte[] PAGE_2_HEADER = new byte[] { 0x01, 0x02, 0x00, 0x01, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    /**
     * Page size that can be sent to the ESD at once
     */
    public static final int PAGE_PACKET_SIZE = 8190;

    /**
     * Page 1 for the image command
     */
    private byte[] p1 = new byte[PAGE_PACKET_SIZE];

    /**
     * Page 2 for the image command
     */
    private byte[] p2 = new byte[PAGE_PACKET_SIZE];

    /**
     * Pixels(times 3 to get the amount of bytes) of an icon that can be sent with page 1 of the image command
     */
    public static final int NUM_FIRST_PAGE_PIXELS = 2583;

    /**
     * Pixels(times 3 to get the amount of bytes) of an icon that can be sent with page 2 of the image command
     */
    public static final int NUM_SECOND_PAGE_PIXELS = 2601;

    /**
     * Icon size of one key
     */
    public static final int ICON_SIZE = 72;

    public void sdcReset() {
        hidDevice.setFeatureReport(RESET_DATA[0], Arrays.copyOfRange(RESET_DATA, 1, RESET_DATA.length), RESET_DATA.length-1);
    }

    public synchronized void sdcColor() {
        internalDrawImage(0, Color.RED);
        internalDrawImage(1, Color.GREEN);
        internalDrawImage(2, Color.BLACK);
        internalDrawImage(3, Color.GRAY);
        internalDrawImage(4, Color.BLACK);

    }

    private synchronized void internalDrawImage(int keyIndex, Color color) {
        byte[] imgData = createColoredIcon(color, ICON_SIZE);
        byte[] page1 = generatePage1(keyIndex, imgData);
        byte[] page2 = generatePage2(keyIndex, imgData);
        this.hidDevice.setOutputReport((byte) 0x02, page1, page1.length);
        this.hidDevice.setOutputReport((byte) 0x02, page2, page2.length);
    }

    /**
     * Generates HID-Report Page 1/2 to update an image of one stream deck key
     *
     * @param keyId
     *            Id of the key to be updated
     * @param imgData
     *            image data in the bgr-format
     * @return HID-Report in byte format ready to be send to the stream deck
     */
    private byte[] generatePage1(int keyId, byte[] imgData) {
        for (int i = 0; i < PAGE_1_HEADER.length; i++) {
            p1[i] = PAGE_1_HEADER[i];
        }
        if (imgData != null) {
            for (int i = 0; i < imgData.length && i < NUM_FIRST_PAGE_PIXELS * 3; i++) {
                p1[PAGE_1_HEADER.length + i] = imgData[i];
            }
        }
        p1[4] = (byte) (keyId + 1);
        return p1;
    }

    /**
     * Generates HID-Report Page 2/2 to update an image of one stream deck key
     *
     * @param keyId
     *            Id of the key to be updated
     * @param imgData
     *            image data in the bgr-format
     * @return HID-Report in byte format ready to be send to the stream deck
     */
    private byte[] generatePage2(int keyId, byte[] imgData) {
        for (int i = 0; i < PAGE_2_HEADER.length; i++) {
            p2[i] = PAGE_2_HEADER[i];
        }
        if (imgData != null) {
            for (int i = 0; i < NUM_SECOND_PAGE_PIXELS * 3 && i < imgData.length; i++) {
                p2[PAGE_2_HEADER.length + i] = imgData[(NUM_FIRST_PAGE_PIXELS * 3) + i];
            }
        }
        p2[4] = (byte) (keyId + 1);
        return p2;
    }

    private static byte[] createColoredIcon_defunct(Color color, int iconSize) {
        BufferedImage img = new BufferedImage(iconSize, iconSize, /*BufferedImage.TYPE_INT_ARGB*/BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, iconSize, iconSize);
        byte[] bytes = bufferedImageToByteArrayData(img, iconSize);
        g.dispose();
        return bytes;
    }

    private static byte[] createColoredIcon(Color color, int iconSize) {
        int width = iconSize;
        int height = iconSize;
        BufferedImage off_Image =
                new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = off_Image.createGraphics();
        g2.setColor(color);
        g2.fillRect(0, 0, width, height);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(off_Image, "jpg", bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("write failed");
        }
    }


    private static byte[] bufferedImageToByteArrayData(BufferedImage img, int iconSize) {
        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        byte[] imgData = new byte[iconSize * iconSize * 3];
        int imgDataCount = 0;
        // remove the alpha channel
        for (int i = 0; i < iconSize * iconSize; i++) {
            // RGB -> BGR
            imgData[imgDataCount++] = (byte) ((pixels[i] >> 16) & 0xFF);
            imgData[imgDataCount++] = (byte) (pixels[i] & 0xFF);
            imgData[imgDataCount++] = (byte) ((pixels[i] >> 8) & 0xFF);
        }

        return imgData;
    }


    //-----------------------------------------------------------------------------------------------------

    public void riColor1() {
        riColor((byte)0, Color.RED);
    }

    public void riColor() {
        riColor((byte)0, Color.RED);
        riColor((byte)1, Color.GREEN);
        riColor((byte)2, Color.ORANGE);
        riColor((byte)3, Color.GRAY);
        riColor((byte)4, Color.BLACK);
        riColor((byte)5, Color.BLUE);
        riColor((byte)6, Color.CYAN);
        riColor((byte)7, Color.MAGENTA);

        riColor((byte)8, Color.RED);
        riColor((byte)9, Color.GREEN);
        riColor((byte)10, Color.ORANGE);
        riColor((byte)11, Color.GRAY);
        riColor((byte)12, Color.BLACK);
        riColor((byte)13, Color.BLUE);
        riColor((byte)14, Color.CYAN);
    }

    private void riColor(byte buttonIndex, Color color) {
        int imageReportLength = /*1024*/1023;
        int imageReportHeaderLength = 8;
        int imageReportPayloadLength = imageReportLength - imageReportHeaderLength;
        byte[] buttonImage = createColoredIcon(color, 72);
        writeByteArrayToFile(buttonImage, "/tmp/bi.data");

        //----------
        int iteration = 0;
        int payloadLength = buttonImage.length;
        int remainingBytes = payloadLength;

        try {
            while (remainingBytes > 0) {
                int sliceLength = Math.min(remainingBytes, imageReportPayloadLength);
                int bytesSent = iteration * imageReportLength;//TODO MMUCHA: maybe +1??
                boolean isLastPacket = sliceLength == remainingBytes;


//            byte finalizer = sliceLength == remainingBytes ? (byte)1 : (byte)0;

                // These components are nothing else but UInt16 low-endian
                // representations of the length of the image payload, and iteration.
                byte bitmaskedLength = (byte)(sliceLength & 0xFF);
                byte shiftedLength = (byte)(sliceLength >> 8);
                byte bitmaskedIteration = (byte)(iteration & 0xFF);
                byte shiftedIteration = (byte)(iteration >> 8);


                byte[] header = new byte[]{
//                    0x02, //it seems, that this is written by our hid library, so we must not write it here.
                        0x07,
                        buttonIndex,
                        (byte) (isLastPacket ? 1 : 0),
                        bitmaskedLength,
                        shiftedLength,
                        bitmaskedIteration,
                        shiftedIteration};
//            var payload = header.Concat(new ArraySegment<byte>(content, bytesSent, sliceLength)).ToArray();
//            var padding = new byte[ImageReportLength - payload.Length];

                byte[] finalPayload = new byte[imageReportLength];
                Arrays.fill(finalPayload, (byte)0);
                System.arraycopy(header, 0, finalPayload, 0, header.length);
                System.arraycopy(buttonImage, bytesSent, finalPayload, header.length, sliceLength);

                long start = System.nanoTime();
                int i = this.hidDevice.setOutputReport((byte) 0x02, finalPayload, finalPayload.length);

                long diff = System.nanoTime() - start;
                log.info("writing done: Written {} bytes, writing done in {}ms",i, TimeUnit.NANOSECONDS.toMillis(diff));

                byte[] tmp = new byte[sliceLength];
                System.arraycopy(buttonImage, bytesSent, tmp, 0, sliceLength);
                writeByteArrayToFile(tmp, "/tmp/payload"+iteration+".data");
                remainingBytes -= sliceLength;
                iteration++;
            }

        } catch (Exception e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }


    }

    private void writeByteArrayToFile(byte[] byteArray, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(byteArray);
        } catch (Exception e) {
            System.err.println("argh");
            e.printStackTrace(System.err);
        }
    }

    public static byte[] intAsLittleEndian(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        //IMPORTANT!
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(i);
        return buffer.array();
    }

    public void cc() {
        System.out.println(createColoredIcon(Color.red, 10).length);
    }
}
