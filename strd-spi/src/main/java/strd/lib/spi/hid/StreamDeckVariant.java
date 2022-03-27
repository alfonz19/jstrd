package strd.lib.spi.hid;

import strd.lib.common.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum StreamDeckVariant {
    STREAM_DECK_MINI(new StreamDeckIdentification(0x0063)),
    STREAM_DECK_ORIGINAL(new StreamDeckIdentification(0x0060)),
    STREAM_DECK_ORIGINAL_V2(new StreamDeckIdentification(0x006d)),
    STREAM_DECK_XL(new StreamDeckIdentification(0x006c)),
    STREAM_DECK_MK2(3, 5, 72, new StreamDeckIdentification(0x0080));

    private final int keyCount;
    private final int rowCount;
    private final int columnCount;
    private final int pixelCountPerIconSide;
    private final List<StreamDeckIdentification> ids;

    StreamDeckVariant(int rowCount,
                      int columnCount,
                      int pixelCountPerIconSide,
                      List<StreamDeckIdentification> ids) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.pixelCountPerIconSide = pixelCountPerIconSide;
        this.ids = ids;
        this.keyCount = rowCount * columnCount;
    }

    StreamDeckVariant(int rowCount,
                      int columnCount,
                      int pixelCountPerIconSide,
                      StreamDeckIdentification id) {
        this(rowCount, columnCount, pixelCountPerIconSide, Collections.singletonList(id));
    }

    //TODO MMUCHA: remove.
    /**
     * For devices we know about, but aren't supported yet.
     */
    StreamDeckVariant(StreamDeckIdentification id) {
        this(0, 0, 0, id);
    }

    public static Optional<StreamDeckVariant> valueOf(int vendorId, int productId) {
        return Arrays.stream(StreamDeckVariant.values())
                //filter only supported devices.
                .filter(e -> e.ids.stream().anyMatch(f -> f.productId == productId && f.vendorId == vendorId))
                .findFirst();
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

    public int getPixelCountPerIconSide() {
        return pixelCountPerIconSide;
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
