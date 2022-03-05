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
        HidDeviceInfo info = findStreamDeckDevice();
        printInfoAboutDevice(info);

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
                    log.info("something happened.");
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

            System.out.println("Submit commands.");
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                switch (line) {
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
}
