package strd.lib.hid;

import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import strd.lib.StdrException;
import strd.lib.StreamDeckVariant;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PureJavaHidApi implements HidLibrary {
    @Override
    public List<StreamDeckInfo> findStreamDeckDevices() {
        return purejavahidapi.PureJavaHidApi.enumerateDevices()
                .stream()
                .map(info -> {
                    short vendorId = info.getVendorId();
                    short productId = info.getProductId();
                    String productString = info.getProductString();
                    String serialNumberString = info.getSerialNumberString();
                    return StreamDeckVariant.valueOf(vendorId, productId)
                            .map(streamDeckVariant -> new StreamDeckInfo(vendorId,
                                    productId,
                                    streamDeckVariant,
                                    serialNumberString,
                                    productString,
                                    info));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public StreamDeckHandle openDevice(StreamDeckInfo streamDeckInfo) {
        try {
            HidDeviceInfo internalRepresentation = (HidDeviceInfo) streamDeckInfo.getInternalRepresentation();
            HidDevice openedDevice = purejavahidapi.PureJavaHidApi.openDevice(internalRepresentation);
            PureJavaHidApiStreamDeckHandle result = new PureJavaHidApiStreamDeckHandle(openedDevice);
            result.setDeviceRemovalListener(() -> {/*do nothing*/});
            return result;
        } catch (IOException e) {
            throw new StdrException(e);
        }
    }

    private void onDeviceRemoval(PureJavaHidApiStreamDeckHandle result) {
        result.close();
    }

    private static class PureJavaHidApiStreamDeckHandle implements StreamDeckHandle {

        private HidDevice hidDevice;

        public PureJavaHidApiStreamDeckHandle(HidDevice hidDevice) {
            this.hidDevice = hidDevice;
        }

        @Override
        public void close() {
            assertOpenedDevice();
            hidDevice.close();
            hidDevice = null;
        }

        @Override
        public void setInputReportListener(InputReportListener inputReportListener) {
            assertOpenedDevice();
            hidDevice.setInputReportListener((source, reportID, reportData, reportLength) ->
                    inputReportListener.onInputReport(reportData, reportLength));
        }

        @Override
        public void setDeviceRemovalListener(DeviceRemovalListener deviceRemovalListener) {
            assertOpenedDevice();
            hidDevice.setDeviceRemovalListener(source -> {
                //regardless if user want's or not, this device was removed, so we call close to have up-to-date info, that
                //this device is closed.
                close();
                deviceRemovalListener.onDeviceRemoved();
            });
        }

        private void assertOpenedDevice() {
            if (isClosed()) {
                throw new StdrException("Device already closed");
            }

        }

        @Override
        public boolean isClosed() {
            return hidDevice == null;
        }
    }
}
