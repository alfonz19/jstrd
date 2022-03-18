package strd.lib.hid;

import strd.lib.StreamDeckVariant;

import java.util.List;

public interface HidLibrary {
    List<StreamDeckInfo> findStreamDeckDevices();
    StreamDeckHandle createStreamDeckHandle(StreamDeckInfo streamDeckInfo);

    //TODO MMUCHA: most of this is obtainable from handle! refactor into interface.
    class StreamDeckInfo {
        private final short vendorId;
        private final short productId;
        private final StreamDeckVariant streamDeckVariant;
        private final String serialNumberString;
        private final String productString;
        private final Object internalRepresentation;

        public StreamDeckInfo(short vendorId,
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

        public Object getInternalRepresentation() {
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
