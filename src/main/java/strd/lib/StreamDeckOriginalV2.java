package strd.lib;

import strd.lib.hid.StreamDeckHandle;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamDeckOriginalV2 extends AbstractStreamDeck {

    public static final byte[] RESET_DEVICE_PAYLOAD = createResetDeviceRequest();
    private static final Logger log = LoggerFactory.getLogger(StreamDeckOriginalV2.class);

    public StreamDeckOriginalV2(StreamDeckHandle streamDeckHandle) {
        super(streamDeckHandle);
    }


    @Override
    public String getSerialNumber() {
        byte[] getSerialNumberRequest = createGetSerialNumberRequest();
        streamDeckHandle.getFeatureReport(getSerialNumberRequest, getSerialNumberRequest.length);

        int prefixToSkip = 2;
        String str = new String(getSerialNumberRequest,
                prefixToSkip,
                getSerialNumberRequest.length - prefixToSkip,
                StandardCharsets.UTF_8);
//        int firstNullByte = str.indexOf('\0');
//        return str.substring(0, firstNullByte).trim();
        return str.trim();
    }

    private static byte[] createGetSerialNumberRequest() {
        byte[] payload = new byte[32];
        payload[0] = 0x06;
        return payload;
    }

    @Override
    public void resetDevice() {
        int i = this.streamDeckHandle.setFeatureReport((byte) 0x03, RESET_DEVICE_PAYLOAD, RESET_DEVICE_PAYLOAD.length);
        if (i == -1) {
            log.warn("Resetting device failed");
        } else {
            log.debug("Resetting device returned: {}", i);
        }
    }

    private static byte[] createResetDeviceRequest() {
        byte[] resetPayload = new byte[32];
        resetPayload[0] = 0x02;
        return resetPayload;
    }

    @Override
    public void setBrightnessImpl(int percent) {
        //fix incorrect input.
        percent = (byte)Math.min(Math.max(percent, 0), 100);

        byte[] setBrightnessRequest = createSetBrightnessRequest();
        setBrightnessRequest[1] = (byte)percent;
        this.streamDeckHandle.setFeatureReport((byte) 0x03, setBrightnessRequest, setBrightnessRequest.length);
    }

    private byte[] createSetBrightnessRequest() {
        byte[] payload = new byte[32];
        payload[0] = 0x08;

        //default value, to be updated. Specifying it here just to have valid request to begin with
        payload[1] = (byte)50;

        return payload;
    }

    //-----------------


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

                log.debug("header length={}", header.length);
                log.debug(
                        "sending {}-th packet. Slice length={}, isLastPacketByte={}, bytesAlreadySent={}",
                        iteration,
                        sliceLength,
                        isLastPacketByte,
                        bytesAlreadySent);

                long start = System.nanoTime();
                int i = this.streamDeckHandle.setOutputReport((byte) 0x02, finalPayload, finalPayload.length);

                long diff = System.nanoTime() - start;
                log.trace("writing done: Written {} bytes, writing done in {}ms",
                        i,
                        TimeUnit.NANOSECONDS.toMillis(diff));

                remainingBytes -= sliceLength;
                iteration++;
            }

            log.debug("sent {} packets in total", iteration);

        } catch (Exception e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }
    }

}
