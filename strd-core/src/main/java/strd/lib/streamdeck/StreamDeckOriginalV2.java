package strd.lib.streamdeck;

import strd.lib.spi.hid.StreamDeckHandle;

import java.util.Arrays;
import java.util.List;
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
    protected StreamDeckCommand createResetCommand() {
        return new ResetCommand((byte) 0x03, RESET_DEVICE_PAYLOAD, RESET_DEVICE_PAYLOAD.length);
    }

    private static byte[] createResetDeviceRequest() {
        byte[] resetPayload = new byte[32];
        resetPayload[0] = 0x02;
        return resetPayload;
    }

    @Override
    public SetBrightnessCommand createSetBrightnessCommand(int percent) {
        //fix incorrect input.
        percent = (byte)Math.min(Math.max(percent, 0), 100);

        byte[] payload = new byte[32];
        payload[0] = 0x08;
        payload[1] = (byte)percent;

        return new SetBrightnessCommand(0x03, payload, payload.length);
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
                //it seems, that streamdeck mk2 needs whole packets to be sent each time. Otherwise it fails ...
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
    protected SetButtonImageCommand createSetButtonImageCommand(List<byte[]> payloadsBytes) {
        return new SetButtonImageCommand(0x02, payloadsBytes);
    }
}
