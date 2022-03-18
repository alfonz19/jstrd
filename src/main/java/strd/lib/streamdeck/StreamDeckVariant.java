package strd.lib.streamdeck;

import strd.lib.Constants;
import strd.lib.hid.StreamDeckHandle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public enum StreamDeckVariant {
    STREAM_DECK_MINI(new StreamDeckIdentification(0x0063)),
    STREAM_DECK_ORIGINAL(new StreamDeckIdentification(0x0060)),
    STREAM_DECK_ORIGINAL_V2(new StreamDeckIdentification(0x006d)),
    STREAM_DECK_XL(new StreamDeckIdentification(0x006c)),
    STREAM_DECK_MK2(3, 5, new StreamDeckIdentification(0x0080), StreamDeckOriginalV2::new);

    private final int keyCount;
    private final int rowCount;
    private final int columnCount;
    private final List<StreamDeckIdentification> ids;
    private final Function<StreamDeckHandle, StreamDeck> ctor;

    StreamDeckVariant(int rowCount,
                      int columnCount,
                      List<StreamDeckIdentification> ids,
                      Function<StreamDeckHandle, StreamDeck> ctor) {
        this.keyCount = rowCount * columnCount;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.ids = ids;
        this.ctor = ctor;
    }

    StreamDeckVariant(int rowCount,
                      int columnCount,
                      StreamDeckIdentification id,
                      Function<StreamDeckHandle, StreamDeck> ctor) {
        this(rowCount, columnCount, Collections.singletonList(id), ctor);
    }

    /**
     * For devices we know about, but aren't supported.
     */
    StreamDeckVariant(StreamDeckIdentification id) {
        this(0, 0, id, null);
    }

    public static Optional<StreamDeckVariant> valueOf(int vendorId, int productId) {
        return Arrays.stream(StreamDeckVariant.values())
                //filter only supported devices.
                .filter(e->e.ctor != null)
                .filter(e -> e.ids.stream().anyMatch(f -> f.productId == productId && f.vendorId == vendorId))
                .findFirst();
    }

    public StreamDeck create(StreamDeckHandle streamDeckHandle) {
        return ctor.apply(streamDeckHandle);
    }

    public int getKeyCount() {
        return keyCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    private static class StreamDeckIdentification {
        private final int vendorId;
        private final int productId;

        public StreamDeckIdentification(int vendorId, int productId) {
            this.vendorId = vendorId;
            this.productId = productId;
        }

        public StreamDeckIdentification(int productId) {
            this(Constants.PRODUCT_ID, productId);
        }
    }
}
