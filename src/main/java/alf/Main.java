package alf;

import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;
import purejavahidapi.PureJavaHidApi;

import java.io.IOException;
import java.rmi.ServerError;
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
                switch (line) {
                    case "quit": break outer;
                    default:
                        System.out.println("unknown command");
                }
            }

//            try {
//                while (!terminate) {
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
//                }
//
//                log.info("main thread done.");
//            } catch (Exception e) {
//                throw new RuntimeException("whoa, what was that", e);
//            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to open", e);
        }


//---------------------
//        try {
//            hidDevice = PureJavaHidApi.openDevice(info);
//            try {
//
//            } finally {
//                try {
//                    hidDevice.close();
//                } catch (Exception e) {
//                    log.error("error closing.");
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Unable to open", e);
//        }
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
}
