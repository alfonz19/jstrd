package strd.lib.streamdeck;

import strd.lib.hid.StreamDeckHandle;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamDeckOriginalV2 extends AbstractStreamDeck {

    private static final Logger log = LoggerFactory.getLogger(StreamDeckOriginalV2.class);

    public static final byte[] RESET_DEVICE_PAYLOAD = createResetDeviceRequest();

    //this can vary per device.
    private static final int MAX_PACKET_SIZE = 1024;
    //total size of header, in packet actually sent, ie. when whole header is there, including reportID.
    private static final int IMAGE_REPORT_HEADER_LENGTH = 8;

    //max amount of actual actual image data sent in individual packet.
    private static final int MAX_IMAGE_DATA_SIZE = MAX_PACKET_SIZE - IMAGE_REPORT_HEADER_LENGTH;

    //the first byte is set by HID library we use, and it's not part of data array we pass into it.
    private static final int PACKET_SIZE_WITHOUT_REPORT_ID = MAX_PACKET_SIZE - 1;

    //the first byte is set by HID library we use, and it's not part of data array we pass into it.
    private static final int PACKET_HEADER_SIZE_WITHOUT_REPORT_ID = IMAGE_REPORT_HEADER_LENGTH - 1;

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

    @Override
    public void splitNativeImageBytesAndProcess(int buttonIndex, byte[] buttonImage, BiConsumer<byte[], Integer> processSetImagePayload) {
        if (buttonIndex < 0 || buttonIndex > 14) {
            log.error("Not existing button: {}", buttonIndex);
            return;
        }

        byte buttonIndexAsByte = (byte) buttonIndex;

        //----------
        int iteration = 0;
        int remainingBytes = buttonImage.length;

        try {
            while (remainingBytes > 0) {
                int sliceLength = Math.min(remainingBytes, MAX_IMAGE_DATA_SIZE);
                int bytesAlreadySent = iteration * MAX_IMAGE_DATA_SIZE;
                boolean isLastPacket = sliceLength == remainingBytes;

                // These components are nothing else but UInt16 low-endian
                // representations of the length of the image payload, and iteration.
                byte bitmaskedLength = (byte) (sliceLength & 0xFF);
                byte shiftedLength = (byte) (sliceLength >> 8);

                byte bitmaskedIteration = (byte) (iteration & 0xFF);
                byte shiftedIteration = (byte) (iteration >> 8);

                byte isLastPacketByte = (byte) (isLastPacket ? 1 : 0);

                byte[] payload = new byte[PACKET_SIZE_WITHOUT_REPORT_ID];


                //write header.
                payload[0] = 0x07;
                payload[1] = buttonIndexAsByte;
                payload[2] = isLastPacketByte;
                payload[3] = bitmaskedLength;
                payload[4] = shiftedLength;
                payload[5] = bitmaskedIteration;
                payload[6] = shiftedIteration;

                //copy the image data
                System.arraycopy(buttonImage, bytesAlreadySent, payload, PACKET_HEADER_SIZE_WITHOUT_REPORT_ID, sliceLength);
                if (sliceLength < MAX_IMAGE_DATA_SIZE) {
                    Arrays.fill(payload,
                            PACKET_HEADER_SIZE_WITHOUT_REPORT_ID + sliceLength,
                            PACKET_SIZE_WITHOUT_REPORT_ID,
                            (byte) 0);
                }

                log.debug(
                        "sending {}-th packet. Slice length={}, isLastPacketByte={}, bytesAlreadySent={}",
                        iteration,
                        sliceLength,
                        isLastPacketByte,
                        bytesAlreadySent);

                long start = System.nanoTime();
                processSetImagePayload.accept(payload, /*sliceLength+PACKET_HEADER_SIZE_WITHOUT_REPORT_ID*/PACKET_SIZE_WITHOUT_REPORT_ID);

                long diff = System.nanoTime() - start;
                log.trace("writing done in {}ms",
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

    @Override
    public void setButtonImage(byte[][] payloadsBytes) {
        for (byte[] payloadBytes : payloadsBytes) {
            //TODO MMUCHA: logging.
            int result = sendIthPacketSettingButtonImage(payloadBytes, payloadBytes.length);
        }
    }

    private int sendIthPacketSettingButtonImage(byte[] payloadBytes, int length) {
        return this.streamDeckHandle.setOutputReport((byte) 0x02, payloadBytes, length);
    }

    @Override
    public void setButtonImage(int buttonIndex, byte[] buttonImage) {
        splitNativeImageBytesAndProcess(buttonIndex, buttonImage, (bytes, length)-> {
            //TODO MMUCHA: logging.
            int result = sendIthPacketSettingButtonImage(bytes, length);
        });
    }
}
