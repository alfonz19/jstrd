package alf;

import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;
import purejavahidapi.PureJavaHidApi;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
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

    public void test() {
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

            System.out.println("Submit commands(quit): ");
            Scanner scanner = new Scanner(System.in);
            outer: while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

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
        byte[] imgData = createColoredIcon(color);
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

    private static byte[] createColoredIcon(Color color) {
        BufferedImage img = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, ICON_SIZE, ICON_SIZE);
        g.dispose();
        return bufferedImageToByteArrayData(img);
    }

    private static byte[] bufferedImageToByteArrayData(BufferedImage img) {
        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        byte[] imgData = new byte[ICON_SIZE * ICON_SIZE * 3];
        int imgDataCount = 0;
        // remove the alpha channel
        for (int i = 0; i < ICON_SIZE * ICON_SIZE; i++) {
            // RGB -> BGR
            imgData[imgDataCount++] = (byte) ((pixels[i] >> 16) & 0xFF);
            imgData[imgDataCount++] = (byte) (pixels[i] & 0xFF);
            imgData[imgDataCount++] = (byte) ((pixels[i] >> 8) & 0xFF);
        }

        return imgData;
    }
}
