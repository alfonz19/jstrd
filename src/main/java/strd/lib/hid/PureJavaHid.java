package strd.lib.hid;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import strd.lib.StdrException;
import strd.lib.StreamDeckVariant;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PureJavaHid implements HidLibrary {

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
                            .map(streamDeckVariant -> new StreamDeckInfoImpl(
                                    streamDeckVariant,
                                    info));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public StreamDeckHandle createStreamDeckHandle(StreamDeckInfo streamDeckInfo) {
        try {
            HidDeviceInfo internalRepresentation = ((StreamDeckInfoImpl) streamDeckInfo).getInternalRepresentation();
            HidDevice openedDevice = purejavahidapi.PureJavaHidApi.openDevice(internalRepresentation);
            PureJavaHidApiStreamDeckHandle result = new PureJavaHidApiStreamDeckHandle(openedDevice, streamDeckInfo);
            result.setDeviceRemovalListener(e -> {/*do nothing*/});
            return result;
        } catch (IOException e) {
            throw new StdrException(e);
        }
    }

    private static class PureJavaHidApiStreamDeckHandle implements StreamDeckHandle {

        private HidDevice hidDevice;
        private final StreamDeckInfo streamDeckInfo;

        public PureJavaHidApiStreamDeckHandle(HidDevice hidDevice, StreamDeckInfo streamDeckInfo) {
            this.hidDevice = hidDevice;
            this.streamDeckInfo = streamDeckInfo;
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
                deviceRemovalListener.onDeviceRemoved(streamDeckInfo);
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

        @Override
        public int getFeatureReport(byte[] requestBytes, int length) {
            return hidDevice.getFeatureReport(requestBytes, length);
        }

        @Override
        public int setFeatureReport(byte reportId, byte[] data, int length) {
            return hidDevice.setFeatureReport(reportId, data, length);
        }

        @Override
        public int setOutputReport(byte reportId, byte[] data, int length) {
            return hidDevice.setOutputReport(reportId, data, length);
        }

        @Override
        public StreamDeckInfo getStreamDeckInfo() {
            return streamDeckInfo;
        }
    }

    public static class StreamDeckInfoImpl implements StreamDeckInfo {
        private final short vendorId;
        private final short productId;
        private final StreamDeckVariant streamDeckVariant;
        private final String serialNumberString;
        private final String productString;
        private final HidDeviceInfo internalRepresentation;

        public StreamDeckInfoImpl(StreamDeckVariant streamDeckVariant, HidDeviceInfo hidDeviceInfo) {
            this.streamDeckVariant = streamDeckVariant;
            this.vendorId = hidDeviceInfo.getVendorId();
            this.productId = hidDeviceInfo.getProductId();
            this.serialNumberString = hidDeviceInfo.getSerialNumberString();
            this.productString = hidDeviceInfo.getProductString();
            this.internalRepresentation = hidDeviceInfo;
        }

        @Override
        public short getVendorId() {
            return vendorId;
        }

        @Override
        public short getProductId() {
            return productId;
        }

        @Override
        public StreamDeckVariant getStreamDeckVariant() {
            return streamDeckVariant;
        }

        public HidDeviceInfo getInternalRepresentation() {
            return internalRepresentation;
        }

        @Override
        public String getSerialNumberString() {
            return serialNumberString;
        }

        @Override
        public String getProductString() {
            return productString;
        }

        @Override
        public String toString() {
            return "StreamDeckInfo {\n" + "\tvendorId=" + vendorId + ",\n" +
                    "\tproductId=" + productId + ",\n" +
                    "\tstreamDeckVariant=" + streamDeckVariant + ",\n" +
                    "\tserialNumberString='" + serialNumberString + '\'' + ",\n" +
                    "\tproductString='" + productString + '\'' + "\n" +
                    '}';
        }
    }
}