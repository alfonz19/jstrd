package strd.lib.iconpainter.factory;

import strd.lib.streamdeck.StreamDeckVariant;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBufferedImageIconPainterFactory implements IconPainterFactory {

    private static final Logger log = LoggerFactory.getLogger(AbstractBufferedImageIconPainterFactory.class);
    protected final Set<StreamDeckVariant> supportedVariants;

    public AbstractBufferedImageIconPainterFactory(StreamDeckVariant... supportedVariants) {
        this.supportedVariants = Arrays.stream(supportedVariants).collect(Collectors.toSet());
    }

    @Override
    public boolean canProcessStreamDeckVariant(StreamDeckVariant streamDeckVariant) {
        return supportedVariants.contains(streamDeckVariant);
    }
}
