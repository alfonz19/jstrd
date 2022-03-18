package alf;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import ij.ImagePlus;
import purejavahidapi.HidDeviceInfo;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainUsingHid4Java {
    private static final Logger log = LoggerFactory.getLogger(MainUsingHid4Java.class);
    private static final Logger hidCommunicationLogger = LoggerFactory.getLogger("HID");


    public static final int VENDOR_ID = 4057;
    public static final int PRODUCT_ID = 128;
    public static final int ICON_SIZE = 72;
    public static final String TEST_IMAGE_JPG = "/testImage.jpg";
    private boolean terminate = false;
    private HidDevice hidDevice;


    public static void main(String[] args) {
        new MainUsingHid4Java().test();
    }

    private void test() {
        // Configure to use custom specification
        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
        // Use the v0.7.0 manual start feature to get immediate attach events
        hidServicesSpecification.setAutoStart(false);

        // Get HID services using custom specification
        HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);

        this.hidDevice = hidServices.getAttachedHidDevices().stream()
                .filter(hidDevice -> hidDevice.getVendorId() == 4057 &&
                        hidDevice.getProductId() == 128 &&
                        !hidDevice.isOpen())
                .findFirst().orElseThrow(() -> new RuntimeException("Unable to find my device"));

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
            hidDevice.open();
            log.info("device opened.");

            ...well there isn't input listener, I'd have to write it on my own...
            hidDevice.setInputReportListener(new InputReportListener() {
                @Override
                public void onInputReport(HidDevice source, byte reportID, byte[] reportData, int reportLength) {

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

            System.out.println(Arrays.stream(MainUsingHid4Java.class.getDeclaredMethods()).filter(e->Modifier.isPublic(e.getModifiers())).filter(e->e.getParameterTypes().length ==0).map(Method::getName).collect(Collectors.joining("\n", "existing public no-arg methods: \n", "")));
            System.out.println("Submit commands(quit): ");
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.isEmpty()) continue;
                if (line.equals("quit")) { break; }

                try {
                    Method method = MainUsingHid4Java.class.getMethod(line);
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

//    private HidDeviceInfo findStreamDeckDevice() {
//
//        List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
//        log.info("number of devices: {}", devList.size());
//        for (HidDeviceInfo info : devList) {
//            log.debug("Vendor-ID: " + Integer.toHexString((int)info.getVendorId()) + ", Product-ID: " + Integer.toHexString((int)info.getProductId()));
//            if (info.getVendorId() == VENDOR_ID && info.getProductId() == PRODUCT_ID) {
//                log.info("Found ESD ["+info.getVendorId()+":"+info.getProductId()+"]");
//                return info;
//            }
//        }
//
//        log.error("Streamdeck not found");
//        System.exit(1);
//        return null;
//    }

//    private HidDeviceInfo findStreamDeckDevice_noLog() {
//
//        List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
//        for (HidDeviceInfo info : devList) {
//            if (info.getVendorId() == VENDOR_ID && info.getProductId() == PRODUCT_ID) {
//                return info;
//            }
//        }
//
//        log.error("Streamdeck not found");
//        System.exit(1);
//        return null;
//    }

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

    /**
     * sets some color to every button
     */
    public void color() {
        color((byte)0, Color.RED);
        color((byte)1, Color.GREEN);
        color((byte)2, Color.ORANGE);
        color((byte)3, Color.GRAY);
        color((byte)4, Color.WHITE);
        color((byte)5, Color.BLUE);
        color((byte)6, Color.CYAN);
        color((byte)7, Color.MAGENTA);

        color((byte)8, Color.RED);
        color((byte)9, Color.GREEN);
        color((byte)10, Color.ORANGE);
        color((byte)11, Color.GRAY);
        color((byte)12, Color.WHITE);
        color((byte)13, Color.BLUE);
        color((byte)14, Color.CYAN);
    }

    /**
     * setups the button image.
     */
    private void color(byte buttonIndex, Color color) {
        byte[] buttonImage = createColoredIcon(color);
        setButtonImage(buttonIndex, buttonImage);


    }

    private void setButtonImage(byte buttonIndex, byte[] buttonImage) {
        if (buttonIndex < 0 || buttonIndex > 14) {
            log.error("Not existing button: {}", buttonIndex);
            return;
        }

        //this can vary per device.
        int maxPacketSize = 1024;
        //total size of header, in packet actually sent, ie. when whole header is there, including reportID.
        int imageReportHeaderLength = 8;

        //max amount of actual actual image data sent in individual packet.
        int maxImageDataSize = maxPacketSize - imageReportHeaderLength;

        //the first byte is set by HID library we use, and it's not part of data array we pass into it.
        int packetSizeWithoutReportID = maxPacketSize - 1;


        //just for debugging, write whole image data to the file to the file.
        writeByteArrayToFile(buttonImage, "/tmp/bi.data");

        //----------
        int iteration = 0;
        int payloadLength = buttonImage.length;
        int remainingBytes = payloadLength;

        try {
            while (remainingBytes > 0) {
                int sliceLength = Math.min(remainingBytes, maxImageDataSize);
                int bytesAlreadySent = iteration * maxImageDataSize;//TODO MMUCHA: do we send just previous bytes, or do we include this packet img data size??? Not sure about this value.
                boolean isLastPacket = sliceLength == remainingBytes;

                // These components are nothing else but UInt16 low-endian
                // representations of the length of the image payload, and iteration.
                byte bitmaskedLength = (byte)(sliceLength & 0xFF);
                byte shiftedLength = (byte)(sliceLength >> 8);

                byte bitmaskedIteration = (byte)(iteration & 0xFF);
                byte shiftedIteration = (byte)(iteration >> 8);


                byte isLastPacketByte = (byte) (isLastPacket ? 1 : 0);
                byte[] header = new byte[]{
//                    0x02, //it seems, that this is written by our hid library, so we must not write it here.
                        0x07,
                        buttonIndex,
                        isLastPacketByte,
                        bitmaskedLength,
                        shiftedLength,
                        bitmaskedIteration,
                        shiftedIteration};
//            var payload = header.Concat(new ArraySegment<byte>(content, bytesAlreadySent, sliceLength)).ToArray();
//            var padding = new byte[ImageReportLength - payload.Length];

                byte[] finalPayload = new byte[packetSizeWithoutReportID];
                Arrays.fill(finalPayload, (byte)0);
                System.arraycopy(header, 0, finalPayload, 0, header.length);
                System.arraycopy(buttonImage, bytesAlreadySent, finalPayload, header.length, sliceLength);

                hidCommunicationLogger.debug("header length={}", header.length);
                hidCommunicationLogger.debug("sending {}-th packet. Slice length={}, isLastPacketByte={}, bytesAlreadySent={}", iteration, sliceLength, isLastPacketByte, bytesAlreadySent);

                long start = System.nanoTime();
                int i = this.hidDevice.setOutputReport((byte) 0x02, finalPayload, finalPayload.length);

                long diff = System.nanoTime() - start;
                hidCommunicationLogger.trace("writing done: Written {} bytes, writing done in {}ms",i, TimeUnit.NANOSECONDS.toMillis(diff));

                //TODO MMUCHA: try to remove.
//                try {
//                    Thread.sleep(2);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }

                byte[] tmp = new byte[sliceLength];
                System.arraycopy(buttonImage, bytesAlreadySent, tmp, 0, sliceLength);
                writeByteArrayToFile(tmp, "/tmp/payload"+iteration+".data");
                remainingBytes -= sliceLength;
                iteration++;
            }

            hidCommunicationLogger.debug("sent {} packets in total", iteration);

        } catch (Exception e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }
    }

    /**
     * will reset device.
     */
    public void resetDevice() {
        byte[] resetPayload = new byte[32];
        Arrays.fill(resetPayload, (byte)0);

        resetPayload[0] = 0x02;

        int i = this.hidDevice.setFeatureReport((byte) 0x03, resetPayload, resetPayload.length);
        log.info("writing reset bytes done, returned: {}", i);
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

    /**
     * not sure how to debug this. this should reset situation from any missing packets;
     * as we're sending updates in multiple packets, if one is missing, the setting
     * of keys will be broken. This should fix it. It's probably very uncommon,
     * and in that case I'd rather do full reset. But yet it is here.
     *
     * Not sure if following is correct, streamdeck-ui uses array which contains only reportID
     */
    public void resetKeyUpdateStream() {
        log.info("resetting");
        int imageReportLength = /*1024*/1023;
        byte[] payload = new byte[imageReportLength];
        Arrays.fill(payload, (byte)0);

        int i = this.hidDevice.setOutputReport((byte) 0x02, payload, payload.length);
        log.info("writing blank key report to reset key stream., returned: {}", i);
    }

    public void setBrightness0() { setBrightness((byte)0);}
    public void setBrightness10() { setBrightness((byte)10);}
    public void setBrightness25() { setBrightness((byte)25);}
    public void setBrightness50() { setBrightness((byte)50);}
    public void setBrightness100() { setBrightness((byte)100);}

    /**
     * Sets the global screen brightness of the StreamDeck, across all the physical buttons.
     */
    private void setBrightness(byte percent) {
        //validate input.
        percent = (byte)Math.min(Math.max(percent, 0), 100);

        byte[] brightnessPayload = new byte[32];
        Arrays.fill(brightnessPayload, (byte)0);

        brightnessPayload[0] = 0x08;
        brightnessPayload[1] = percent;

        int i = this.hidDevice.setFeatureReport((byte) 0x03, brightnessPayload, brightnessPayload.length);
        log.info("writing brightness bytes done, returned: {}", i);
    }

    /**Gets the serial number of the attached StreamDeck.*/
    public void getSerialNumber() {
        byte[] payload = new byte[32];
        payload[0] = 0x06;
        int i = this.hidDevice.getFeatureReport(payload, 32);
        log.info("writing to get serial version done, returned: {}", i);

//        log.info("result data: {}", Arrays.toString(payload));
        byte[] subArray = Arrays.copyOfRange(payload, 2, payload.length);
        log.info("transformed: {}", firmwareOrSerialVersionToHumanString(subArray));

    }
    /**Gets the serial number of the attached StreamDeck.*/
    public void getFirmwareVersion() {
        byte[] payload = new byte[32];
        payload[0] = 0x05;
        int i = this.hidDevice.getFeatureReport(payload, 32);
        log.info("writing to get serial version done, returned: {}", i);

//        log.info("result data: {}", Arrays.toString(payload));
        byte[] subArray = Arrays.copyOfRange(payload, 6, payload.length);
        log.info("transformed: {}", firmwareOrSerialVersionToHumanString(subArray));
    }

    private String firmwareOrSerialVersionToHumanString(byte[] payload) {
        String str = new String(payload);
        int i = str.indexOf('\0');
        return str.substring(0, i).trim();
    }

    public void magda() {
        magda1();
        magda3();
        magda4();
        magda5();
        magda6();
        magda7();
        magda8();
    }

    //dysfunctional  //this just writes image without flipping it twice. wont work.
//    public void magda1() {
//        try {
//            BufferedImage image = readPhotoFromFile("/magda.jpg");
//
//            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
//            Graphics2D g2 = transformed.createGraphics();
//            g2.drawImage(image, 0, 0, null);
//            g2.dispose();
//
//
//            setButtonImage((byte)0, bufferedImageToByteArray(transformed));
//        } catch (Exception e) {
//            log.error("fail", e);
//        }
//    }


    //finally somehow working, but why?
    public void magda1() {
        try {
            BufferedImage image = readPhotoFromFile("/magda.jpg");

            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.drawImage(image, ICON_SIZE, ICON_SIZE, -1*ICON_SIZE, -1*ICON_SIZE, null);
            g2.dispose();


            setButtonImage((byte)0, bufferedImageToByteArray(transformed));
        } catch (Exception e) {
            log.error("fail", e);
        }
    }

    public void testImage() {
        testImage1();
        testImage2();
        testImage3();
        testImage4();
        testImage5();
    }

    //finally somehow working, but why?
    public void testImage1() {
        try {
            BufferedImage image = readPhotoFromFile(TEST_IMAGE_JPG);

            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.drawImage(image, ICON_SIZE, ICON_SIZE, -1*ICON_SIZE, -1*ICON_SIZE, null);
            g2.dispose();


            setButtonImage((byte)10, bufferedImageToByteArray(transformed));
        } catch (Exception e) {
            log.error("fail", e);
        }
    }

    //not working without flipping???
    public void testImage2() {
        try {
            BufferedImage image = readPhotoFromFile(TEST_IMAGE_JPG);

            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.drawImage(image, 0, 0, ICON_SIZE, ICON_SIZE, null);
            g2.dispose();


            setButtonImage((byte)11, bufferedImageToByteArray(transformed));
        } catch (Exception e) {
            log.error("fail", e);
        }
    }
    //not working without flipping???
    public void testImage3() {
        try {
            BufferedImage image = readPhotoFromFile(TEST_IMAGE_JPG);

            setButtonImage((byte)12, bufferedImageToByteArray(image));
        } catch (Exception e) {
            log.error("fail", e);
        }
    }

    public void testImage4() {
        try {
            BufferedImage image = readPhotoFromFile(TEST_IMAGE_JPG);

            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.drawImage(image, 0, 0, ICON_SIZE, ICON_SIZE, null);
            g2.dispose();

            transformed = flipHorizontallyAndVertically(transformed);


            setButtonImage((byte)13, bufferedImageToByteArray(transformed));
        } catch (Exception e) {
            log.error("fail", e);
        }
    }


    public void testImage5() {
        try {
            BufferedImage image = readPhotoFromFile("/testImage_flipped.jpg");

            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.drawImage(image, 0, 0, ICON_SIZE, ICON_SIZE, null);
            g2.dispose();


            setButtonImage((byte)14, bufferedImageToByteArray(transformed));
        } catch (Exception e) {
            log.error("fail", e);
        }
    }

    //finally somehow working, but why?
    public void magda3() {
        try {
            BufferedImage image = readPhotoFromFile("/magda.jpg");

            BufferedImage transformed = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.setClip(0,0,ICON_SIZE,ICON_SIZE);
            g2.drawImage(image, ICON_SIZE, ICON_SIZE, -1*ICON_SIZE, -1*ICON_SIZE, null);
            g2.dispose();


            setButtonImage((byte)2, bufferedImageToByteArray(transformed));
        } catch (Exception e) {
            log.error("fail", e);
        }
    }

    public void magda4() {
        try {
            BufferedImage image = readPhotoFromFile("/magda.jpg");

            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.drawImage(image, ICON_SIZE, ICON_SIZE, -1*ICON_SIZE, -1*ICON_SIZE, null);
            g2.dispose();

            byte[] bytes = writeJpgWithMaxQuality2(transformed);

            setButtonImage((byte)3, bytes);

        } catch (Exception e) {
            log.error("fail", e);
        }
    }
    public void magda5() {
        try {
            BufferedImage image = readPhotoFromFile("/magda.jpg");

            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.drawImage(image, ICON_SIZE, ICON_SIZE, -1*ICON_SIZE, -1*ICON_SIZE, null);
            g2.dispose();

            byte[] bytes = writeJpgWithMaxQuality(transformed);

            setButtonImage((byte)4, bytes);

        } catch (Exception e) {
            log.error("fail", e);
        }
    }

    //finally somehow working, but why?
    public void magda6() {
        try {
            BufferedImage image = readPhotoFromFile("/magda.jpg");

            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.drawImage(image, ICON_SIZE-1, ICON_SIZE-1, -1*ICON_SIZE, -1*ICON_SIZE, null);
            g2.dispose();


            setButtonImage((byte)5, bufferedImageToByteArray(transformed));
        } catch (Exception e) {
            log.error("fail", e);
        }
    }
    //finally somehow working, but why?
    public void magda7() {
        try {
            BufferedImage image = readPhotoFromFile("/magda.jpg");

            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.drawImage(image, ICON_SIZE, ICON_SIZE, -1*ICON_SIZE, -1*ICON_SIZE, null);
            g2.dispose();


            setButtonImage((byte)6, bufferedImageToByteArray(transformed));
        } catch (Exception e) {
            log.error("fail", e);
        }
    }

    //finally somehow working, but why?
    public void magda8() {
        try {
            BufferedImage image = readPhotoFromFile("/magda.jpg");

            BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.drawImage(image, ICON_SIZE, ICON_SIZE, 0, 0, null);
            g2.dispose();


            setButtonImage((byte)7, bufferedImageToByteArray(transformed));
        } catch (Exception e) {
            log.error("fail", e);
        }
    }

    //--------------------------------

    public void drawTest() {
        drawTest1();
        drawTest2();
        drawTest3();
        drawTest4();
        drawTest5();

    }

    //smaller square, non-flipped.
    public void drawTest1() {
        BufferedImage image = createDrawTestSampleImage(20);

        byte[] bytes = bufferedImageToByteArray(image);
        writeBytesToFile(bytes, "/tmp/drawTest.jpg");

        setButtonImage((byte) 5, bytes);
    }

    //smaller square, non-flipped.
    public void drawTest2() {
        BufferedImage image = createDrawTestSampleImage(20);

        BufferedImage flipped = flipHorizontallyAndVertically(image);
        byte[] bytes = bufferedImageToByteArray(flipped);
        writeBytesToFile(bytes, "/tmp/drawTest.jpg");

        setButtonImage((byte) 6, bytes);
    }

    //bigger square, non-flipped.
    public void drawTest3() {
        BufferedImage image = createDrawTestSampleImage(50);

        byte[] bytes = bufferedImageToByteArray(image);
        writeBytesToFile(bytes, "/tmp/drawTest.jpg");

        setButtonImage((byte) 7, bytes);
    }

    //bigger square, non-flipped.
    public void drawTest4() {
        BufferedImage image = createDrawTestSampleImage(50);

        BufferedImage flipped = flipHorizontallyAndVertically(image);
        byte[] bytes = bufferedImageToByteArray(flipped);
        writeBytesToFile(bytes, "/tmp/drawTest.jpg");

        setButtonImage((byte) 8, bytes);
    }

    //bigger square, non-flipped.
    public void drawTest5() {
        BufferedImage image = createDrawTestSampleImage(30);

        BufferedImage flipped = flipHorizontallyAndVertically(image);
        byte[] bytes = bufferedImageToByteArray(flipped);
        writeBytesToFile(bytes, "/tmp/drawTest.jpg");

        setButtonImage((byte) 9, bytes);
    }

    private BufferedImage createDrawTestSampleImage(int squareSize) {
        BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        g2.setClip(0,0, ICON_SIZE, ICON_SIZE);
        g2.setColor(Color.RED);
        g2.setBackground(Color.BLACK);
        g2.fillRect(0, 0, ICON_SIZE, ICON_SIZE);

        g2.setColor(Color.BLUE);
//        g2.fillRect(15, 15, 45, 45);
//        g2.fillRect(15, 15, 5, 5);
        g2.fillRect(15, 15, squareSize, squareSize);
        g2.drawOval(10,10,20,20);

//            g2.setColor(Color.BLACK);
////            g2.setStroke(new BasicStroke(4));
//
//            int lastPixel = ICON_SIZE - 1;
//            g2.drawLine(0, 0, lastPixel, lastPixel);
//            g2.drawLine(lastPixel, 0, 0, lastPixel);

        g2.dispose();
        return image;
    }

    public void magda15A() {
        String srcImage = "/15test.jpg";
        float separatorSize = 14.40f;

        BufferedImage image = readPhotoFromFile(srcImage);
        magda15(separatorSize, image);
    }

    public void magda15B() {
        log.info("Please wait several seconds, poorImageIO jpeg reading performance");
        float separatorSize = 20f;

        long start = System.nanoTime();
        BufferedImage image = readPhotoFromFile(new File("/home/mmucha/projects/streamdeck/streamdeck/src/main/resources/fullImage.jpg"));

        long readingTime = System.nanoTime();
        BufferedImage scaledDown = scaleDownBigImageFor15(image, separatorSize);
        long scalingTimeTime = System.nanoTime();
        byte[] bytes = writeJpgWithMaxQuality(scaledDown);
        writeBytesToFile(bytes, "/tmp/scaledDown.jpg");
        magda15(separatorSize, scaledDown);

        log.info("Reading time: {}ms", TimeUnit.NANOSECONDS.toMillis(readingTime-start));
        log.info("Reading time: {}ms", TimeUnit.NANOSECONDS.toMillis(scalingTimeTime-readingTime));

        log.info("Uff, finally done!");
    }

    private void magda15(float separatorSize, BufferedImage image) {
        try {
            log.info("source has type: {}", image.getType());


            int buttonIndex = 0;
            for(int y = 0; y < 3; y++) {
                for(int x = 0; x < 5; x++) {

                    int xpos = x * Double.valueOf(Math.floor(ICON_SIZE + separatorSize)).intValue();
                    int ypos = y * Double.valueOf(Math.floor(ICON_SIZE + separatorSize)).intValue();
                    log.debug("writing button {} at {}x{}", buttonIndex, xpos, ypos);
                    setButtonImage((byte)buttonIndex++, getImageBytesForButton(image, xpos, ypos));
                    log.trace("---");

                }
            }

        } catch (Exception e) {
            log.error("fail", e);
        }
    }

    private byte[] getImageBytesForButton(BufferedImage sourceImage, int x, int y) {
        BufferedImage buttonImage = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = buttonImage.createGraphics();
        g2.drawImage(sourceImage,
                0,
                0,
                ICON_SIZE,
                ICON_SIZE,
                x,
                y,
                x+ICON_SIZE,
                y+ICON_SIZE,
                Color.BLACK,
                (img, infoflags, xx, yy, width, height) -> false);
        g2.dispose();

        return writeJpgWithMaxQuality(flipHorizontallyAndVertically(buttonImage));
    }

    private byte[] bufferedImageToByteArray(BufferedImage buttonImage) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(buttonImage, "jpg", bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("write failed");
        }
    }

    private BufferedImage readPhotoFromFile(String name)  {
        try {
            InputStream resourceAsStream = MainUsingHid4Java.class.getResourceAsStream(name);
            if (resourceAsStream == null) {
                throw new RuntimeException("unable to read file");
            }
            return ImageIO.read(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage readPhotoFromFile(File file)  {
        ImageIO.setUseCache(false);
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readPhotoFromFile2()  {
//        FileInfo fi = new FileInfo();
//
//        Object o = new ImageReader(fi).readPixels(
//                );
        long start = System.nanoTime();
        ImagePlus imagePlus =
                new ImagePlus("file:///home/mmucha/projects/streamdeck/streamdeck/src/main/resources/fullImage.jpg");

        BufferedImage bufferedImage = imagePlus.getBufferedImage();
        long end = System.nanoTime();
        System.out.println("duration: "+(TimeUnit.NANOSECONDS.toMillis(end-start)));
        writeBytesToFile(writeJpgWithMaxQuality(bufferedImage), "/tmp/test");

        //---------------

        start = System.nanoTime();
        ImageIO.setUseCache(false);
        try {
            ImageIO.read(new URL("file:///home/mmucha/projects/streamdeck/streamdeck/src/main/resources/fullImage.jpg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        end = System.nanoTime();
        System.out.println("duration: "+(TimeUnit.NANOSECONDS.toMillis(end-start)));

        //------------
        start = System.nanoTime();
        ImageIO.setUseCache(false);
        try {
            InputStream resourceAsStream = MainUsingHid4Java.class.getResourceAsStream("/fullImage.jpg");
            ImageIO.read(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        end = System.nanoTime();
        System.out.println("duration: "+(TimeUnit.NANOSECONDS.toMillis(end-start)));

        //------------
        start = System.nanoTime();
        ImageIO.setUseCache(false);
        try {
            InputStream resourceAsStream = MainUsingHid4Java.class.getResourceAsStream("/fullImage.jpg");
            byte[] targetArray = org.apache.commons.io.IOUtils.toByteArray(resourceAsStream);
            ByteArrayInputStream str = new ByteArrayInputStream(targetArray);
            ImageIO.read(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        end = System.nanoTime();
        System.out.println("duration: "+(TimeUnit.NANOSECONDS.toMillis(end-start)));

    }

    private BufferedImage flipHorizontallyAndVertically(BufferedImage image) {
        BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g2 = transformed.createGraphics();
        g2.drawImage(image, ICON_SIZE, ICON_SIZE, -1*ICON_SIZE, -1*ICON_SIZE, null);
        g2.dispose();
        return transformed;
    }

    private byte[] writeImageDefaultAndGetBytes(BufferedImage image, String pathname) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", bos);
            ImageIO.write(image, "jpg", new File(pathname));
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("write failed");
        }
    }

    private void writeBytesToFile(byte[] bytes, String name) {
        try (FileOutputStream fos = new FileOutputStream(name)) {
            fos.write(bytes);
            fos.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] writeJpgWithMaxQuality2(BufferedImage image) {
        try {
            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(1f);

            final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
// specifies where the jpg image has to be written
//        writer.setOutput(new FileImageOutputStream(new File(folder.toString() + "/" + filename + ".jpg")));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.setOutput(new MemoryCacheImageOutputStream(bos));

// writes the file with given compression level
// from your JPEGImageWriteParam instance
            writer.write(null, new IIOImage(image, null, null), jpegParams);
            writer.dispose();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] writeJpgWithMaxQuality(BufferedImage image) {
        try {
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(1.0f);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageOutputStream outputStream = new MemoryCacheImageOutputStream(bos);
            jpgWriter.setOutput(outputStream);
            IIOImage outputImage = new IIOImage(image, null, null);
            jpgWriter.write(null, outputImage, jpgWriteParam);
            jpgWriter.dispose();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage scaleDownBigImageFor15(BufferedImage image, float separator) {
        int dx1 = 370;
        int dy1 = 0;
        int dx2 = 3821;
        int dy2 = 2112;

        int origWidth = image.getWidth();
        int origHeight = image.getHeight();
        double origRatio = origWidth / (double) origHeight;

        ////TODO MMUCHA: insufficient, expects that it can be fixed like this. Better cropping might be needed.
        int fixed_dy2 = Double.valueOf(Math.floor(((dx2 - dx1) / origRatio))).intValue() + dy1;
        dy2 = fixed_dy2;

        int targetWidth = Double.valueOf(Math.floor((ICON_SIZE+separator)*4 + ICON_SIZE)).intValue();
        int targetHeight = Double.valueOf(Math.floor((ICON_SIZE+separator)*2 + ICON_SIZE)).intValue();


        //crop original
        BufferedImage subimage = image.getSubimage(dx1, dy1, dx2 - dx1, dy2-dx1);


        ResampleOp resizeOp = new ResampleOp(targetWidth, targetHeight);
        resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
        return resizeOp.filter(subimage, null);
    }

    /**
     * This is another bit of a hack and might also change
     */
    public static int[] loadPixelsCrazyFast( BufferedImage img ){
        return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    }

    public void hi() {
        byte[] whiteImage = createColoredIcon(Color.LIGHT_GRAY);
        byte[] blackImage = createColoredIcon(Color.BLACK);
        IntStream.range(0, 15).forEach(e-> {
            boolean white = e == 6 || e % 5 == 0 || e % 5 == 2 || e % 5 == 4;
            setButtonImage((byte)e, white ? whiteImage : blackImage);
        });


    }
}
