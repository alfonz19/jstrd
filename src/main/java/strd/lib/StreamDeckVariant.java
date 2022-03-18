package strd.lib;

import strd.lib.hid.StreamDeckHandle;
import strd.lib.hid.StreamDeckInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public enum StreamDeckVariant {
    STREAM_DECK_MINI(new StreamDeckIdentification(0x0063)),
    STREAM_DECK_ORIGINAL(new StreamDeckIdentification(0x0060)),
    STREAM_DECK_ORIGINAL_V2(new StreamDeckIdentification(0x006d)),
    STREAM_DECK_XL(new StreamDeckIdentification(0x006c)),
    STREAM_DECK_MK2(new StreamDeckIdentification(0x0080), StreamDeckOriginalV2::new);

    private final List<StreamDeckIdentification> ids;
    private final BiFunction<StreamDeckInfo, StreamDeckHandle, StreamDeck> ctor;

    StreamDeckVariant(List<StreamDeckIdentification> ids, BiFunction<StreamDeckInfo, StreamDeckHandle, StreamDeck> ctor) {
        this.ids = ids;
        this.ctor = ctor;
    }

    StreamDeckVariant(StreamDeckIdentification id, BiFunction<StreamDeckInfo, StreamDeckHandle, StreamDeck> ctor) {
        this(Collections.singletonList(id), ctor);
    }

    /**
     * For devices we know about, but aren't supported.
     */
    StreamDeckVariant(StreamDeckIdentification id) {
        this(id, null);
    }

    public static Optional<StreamDeckVariant> valueOf(int vendorId, int productId) {
        return Arrays.stream(StreamDeckVariant.values())
                //filter only supported devices.
                .filter(e->e.ctor != null)
                .filter(e -> e.ids.stream().anyMatch(f -> f.productId == productId && f.vendorId == vendorId))
                .findFirst();
    }

    public StreamDeck create(StreamDeckInfo streamDeckInfo, StreamDeckHandle streamDeckHandle) {
        return ctor.apply(streamDeckInfo, streamDeckHandle);
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
