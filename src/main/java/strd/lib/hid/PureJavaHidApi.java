package strd.lib.hid;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import strd.lib.StdrException;
import strd.lib.StreamDeckVariant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PureJavaHidApi implements HidLibrary {

    private static final Logger log = LoggerFactory.getLogger(PureJavaHidApi.class);
    private static final Logger hidCommunicationLogger = LoggerFactory.getLogger("HID");

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

    //TODO MMUCHA: extract superclass. Creation of requests can be generalized regardless of imp. etc.
    private static class PureJavaHidApiStreamDeckHandle implements StreamDeckHandle {

        private static final byte[] SET_BRIGHTNESS_REQUEST = createSetBrightnessRequest();
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

        @Override
        public String getSerialNumber() {
            assertOpenedDevice();
            byte[] getSerialNumberRequest = createGetSerialNumberRequest();
            int i = this.hidDevice.getFeatureReport(getSerialNumberRequest, getSerialNumberRequest.length);
            log.trace("Request to get serial version returned: {}", i);

            int prefixToSkip = 2;
            String str = new String(getSerialNumberRequest,
                    prefixToSkip,
                    getSerialNumberRequest.length - prefixToSkip,
                    StandardCharsets.UTF_8);
            int i1 = str.indexOf('\0');
            return str.substring(0, i1).trim();
        }

        @Override
        public void setButtonImage(int buttonIndex, byte[] buttonImage) {
            if (buttonIndex < 0 || buttonIndex > 14) {
                log.error("Not existing button: {}", buttonIndex);
                return;
            }

            byte buttonIndexAsByte = (byte) buttonIndex;

            //this can vary per device.
            int maxPacketSize = 1024;
            //total size of header, in packet actually sent, ie. when whole header is there, including reportID.
            int imageReportHeaderLength = 8;

            //max amount of actual actual image data sent in individual packet.
            int maxImageDataSize = maxPacketSize - imageReportHeaderLength;

            //the first byte is set by HID library we use, and it's not part of data array we pass into it.
            int packetSizeWithoutReportID = maxPacketSize - 1;

            //----------
            int iteration = 0;
            int remainingBytes = buttonImage.length;

            try {
                while (remainingBytes > 0) {
                    int sliceLength = Math.min(remainingBytes, maxImageDataSize);
                    int bytesAlreadySent = iteration * maxImageDataSize;
                    boolean isLastPacket = sliceLength == remainingBytes;

                    // These components are nothing else but UInt16 low-endian
                    // representations of the length of the image payload, and iteration.
                    byte bitmaskedLength = (byte) (sliceLength & 0xFF);
                    byte shiftedLength = (byte) (sliceLength >> 8);

                    byte bitmaskedIteration = (byte) (iteration & 0xFF);
                    byte shiftedIteration = (byte) (iteration >> 8);


                    byte isLastPacketByte = (byte) (isLastPacket ? 1 : 0);
                    byte[] header = new byte[]{
//                    0x02, //it seems, that this is written by our hid library, so we must not write it here.
                            0x07,
                            buttonIndexAsByte,
                            isLastPacketByte,
                            bitmaskedLength,
                            shiftedLength,
                            bitmaskedIteration,
                            shiftedIteration};

                    byte[] finalPayload = new byte[packetSizeWithoutReportID];
                    Arrays.fill(finalPayload, (byte) 0);
                    System.arraycopy(header, 0, finalPayload, 0, header.length);
                    System.arraycopy(buttonImage, bytesAlreadySent, finalPayload, header.length, sliceLength);

                    hidCommunicationLogger.debug("header length={}", header.length);
                    hidCommunicationLogger.debug(
                            "sending {}-th packet. Slice length={}, isLastPacketByte={}, bytesAlreadySent={}",
                            iteration,
                            sliceLength,
                            isLastPacketByte,
                            bytesAlreadySent);

                    long start = System.nanoTime();
                    int i = this.hidDevice.setOutputReport((byte) 0x02, finalPayload, finalPayload.length);

                    long diff = System.nanoTime() - start;
                    hidCommunicationLogger.trace("writing done: Written {} bytes, writing done in {}ms",
                            i,
                            TimeUnit.NANOSECONDS.toMillis(diff));

                    remainingBytes -= sliceLength;
                    iteration++;
                }

                hidCommunicationLogger.debug("sent {} packets in total", iteration);

            } catch (Exception e) {
                System.err.println("failed");
                e.printStackTrace(System.err);
            }
        }

        @Override
        public void resetDevice() {
            byte[] resetPayload = createResetDeviceRequest();

            int i = this.hidDevice.setFeatureReport((byte) 0x03, resetPayload, resetPayload.length);
            if (i == -1) {
                log.warn("Resetting device failed");
            } else {
                log.debug("Resetting device returned: {}", i);
            }
        }

        private byte[] createResetDeviceRequest() {
            byte[] resetPayload = new byte[32];
            resetPayload[0] = 0x02;
            return resetPayload;
        }

        @Override
        public void setBrightness(int percent) {
            //fix incorrect input.
            percent = (byte)Math.min(Math.max(percent, 0), 100);

            SET_BRIGHTNESS_REQUEST[1] = (byte)percent;
            this.hidDevice.setFeatureReport((byte) 0x03, SET_BRIGHTNESS_REQUEST, SET_BRIGHTNESS_REQUEST.length);

        }

        private static byte[] createGetSerialNumberRequest() {
            byte[] payload = new byte[32];
            payload[0] = 0x06;
            return payload;
        }

        private static byte[] createSetBrightnessRequest() {
            byte[] payload = new byte[32];
            payload[0] = 0x08;

            //default value, to be updated. Specifying it here just to have valid request to begin with
            payload[1] = (byte)50;

            return payload;
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
