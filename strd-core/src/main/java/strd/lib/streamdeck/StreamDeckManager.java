package strd.lib.streamdeck;

import strd.jstrd.util.ServiceLoaderUtil;
import strd.lib.common.exception.CannotHappenException;
import strd.lib.spi.hid.HidLibrary;
import strd.lib.spi.hid.StreamDeckHandle;
import strd.lib.spi.hid.StreamDeckVariant;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class StreamDeckManager {
    private static final Logger log = getLogger(StreamDeckManager.class);

    private static final Map<StreamDeckVariant, StreamDeckFactory> STREAM_DECK_FACTORIES =
            ServiceLoaderUtil.loadInstances(StreamDeckFactory.class)
                    .collect(Collectors.toMap(StreamDeckFactory::creates, Function.identity()));

    private final HidLibrary hidLibrary;

    public StreamDeckManager(HidLibrary hidLibrary) {
        this.hidLibrary = hidLibrary;
    }

    public List<HidLibrary.StreamDeckInfo> findStreamDeckDevices() {
        return hidLibrary.findStreamDeckDevices()
                .stream()
                //filter out unsupported stream decks.
                .filter(streamDeckInfo -> {
                    StreamDeckVariant streamDeckVariant = streamDeckInfo.getStreamDeckVariant();
                    boolean supported = STREAM_DECK_FACTORIES.containsKey(streamDeckVariant);
                    if (!supported) {
                        log.warn("StreamDeck variant {}, identified by {}x{} is not yet supported",
                                streamDeckVariant,
                                streamDeckInfo.getVendorId(),
                                streamDeckInfo.getProductId());
                    }
                    return supported;
                })
                .collect(Collectors.toList());
    }

    public StreamDeckDevice openConnection(HidLibrary.StreamDeckInfo streamDeckInfo) {
        StreamDeckHandle streamDeckHandle = hidLibrary.createStreamDeckHandle(streamDeckInfo);
        StreamDeckVariant streamDeckVariant = streamDeckInfo.getStreamDeckVariant();

        if (!STREAM_DECK_FACTORIES.containsKey(streamDeckVariant)) {
            throw new CannotHappenException("Attempt to create unsupported variant");
        } else {
            return STREAM_DECK_FACTORIES.get(streamDeckVariant).create(streamDeckHandle);
        }
    }

    public void addListener(HidLibrary.DeviceListener listener) {
        hidLibrary.addListener(listener);
    }


    public void removeListener(HidLibrary.DeviceListener listener) {
        hidLibrary.removeListener(listener);
    }

    public void removeListeners() {
        hidLibrary.removeListeners();
    }

    public void setPollingInterval(Duration duration) {
        hidLibrary.setPollingInterval(duration);
    }

}
