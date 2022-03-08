package alf;

import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;
import purejavahidapi.PureJavaHidApi;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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
            byte[] bytes = bos.toByteArray();
            g2.dispose();
            return bytes;
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

    //---------------------------------------------------------
    /*






    def get_serial_number(self):
        """
        Gets the serial number of the attached StreamDeck.

        :rtype: str
        :return: String containing the serial number of the attached device.
        """

        serial = self.device.read_feature(0x06, 32)
        return self._extract_string(serial[2:])

    def get_firmware_version(self):
        """
        Gets the firmware version of the attached StreamDeck.

        :rtype: str
        :return: String containing the firmware version of the attached device.
        """

        version = self.device.read_feature(0x05, 32)
        return self._extract_string(version[6:])




    * */
}
