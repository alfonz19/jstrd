package strd.lib.hid;

import strd.lib.StreamDeckVariant;

import java.util.List;

public interface HidLibrary {
    List<StreamDeckInfo> findStreamDeckDevices();

    StreamDeckHandle openDevice(StreamDeckInfo streamDeckInfo);

    interface StreamDeckHandle {
        boolean isClosed();
        void close();

        void setInputReportListener(InputReportListener buttonListener);
        void setDeviceRemovalListener(DeviceRemovalListener deviceRemovalListener);

        /**
         * Issue request to device to fetch its serial number. Probably pointless, as
         * {@link HidLibrary.StreamDeckInfo#getSerialNumberString} seems to return the same string.
         * @return serial number of device
         */
        String getSerialNumber();

        void setButtonImage(int buttonIndex, byte[] buttonImage);
        void resetDevice();
        void setBrightness(int percent);

        interface InputReportListener {
            void onInputReport(byte[] reportData, int reportLength);
        }

        interface DeviceRemovalListener {
            void onDeviceRemoved();
        }
    }

    class StreamDeckInfo {
        private final short vendorId;
        private final short productId;
        private final StreamDeckVariant streamDeckVariant;
        private final String serialNumberString;
        private final String productString;
        private final Object internalRepresentation;

        protected StreamDeckInfo(short vendorId,
                                 short productId,
                                 StreamDeckVariant streamDeckVariant,
                                 String serialNumberString,
                                 String productString,
                                 Object internalRepresentation) {
            this.vendorId = vendorId;
            this.productId = productId;
            this.streamDeckVariant = streamDeckVariant;
            this.serialNumberString = serialNumberString;
            this.productString = productString;
            this.internalRepresentation = internalRepresentation;
        }

        public short getVendorId() {
            return vendorId;
        }

        public short getProductId() {
            return productId;
        }

        public StreamDeckVariant getStreamDeckVariant() {
            return streamDeckVariant;
        }

        protected Object getInternalRepresentation() {
            return internalRepresentation;
        }

        public String getSerialNumberString() {
            return serialNumberString;
        }

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
