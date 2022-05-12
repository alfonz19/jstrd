package strd.jstrd.streamdeck.unfinished.button;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ActionConfiguration;
import strd.jstrd.streamdeck.unfinished.action.Action;
import strd.jstrd.util.FactoryLoader;
import strd.jstrd.util.PropertiesUtil;
import strd.lib.common.exception.CannotHappenException;
import strd.lib.iconpainter.IconPainter;

import java.awt.Color;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ColorButton implements Button {

    private final int red;
    private final int green;
    private final int blue;
    private byte[] iconBytes;

    private final ButtonBehavior bb = new ButtonBehavior();
    //TODO MMUCHA: read from configuration
    private final List<Action> actionList =
            Collections.singletonList(FactoryLoader.findActionFactory("quit").create(new ActionConfiguration("quit")));

    public ColorButton() {
        this(0, 0, 0);
    }

    public ColorButton(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public ColorButton(Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue());
    }

    public ColorButton(StreamDeckConfiguration.ButtonConfiguration buttonConfiguration) {
        this(PropertiesUtil.getColorProperty(buttonConfiguration.findApplicableConditionalButtonConfiguration()
                .map(StreamDeckConfiguration.ConditionalButtonConfiguration::getProperties)
                .orElseThrow(() -> new CannotHappenException("Should be protected by validation")), ColorButtonFactory.COLOR_PROPERTY_NAME));
    }

    @Override
    public void tick(Instant instant) {
        //do nothing
    }

    @Override
    public byte[] draw() {
        return iconBytes;
    }

    @Override
    public void preload(Supplier<IconPainter> iconPainterSupplier) {
        try (IconPainter iconPainter = iconPainterSupplier.get()) {
            iconBytes = iconPainter.fillWholeIcon(red, green, blue).toDeviceNativeFormat();
        }
    }

    @Override
    public boolean needsUpdate() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("Color button r=%s, g=%d, b=%d", red, green, blue);
    }

    @Override
    public void close() {
        //do nothing.
    }

    @Override
    public void updateButtonState(boolean buttonState) {
        bb.buttonUpdated(buttonState, actionList);
    }

    //TODO MMUCHA: move & improve
    public static class ButtonBehavior {
        public void buttonUpdated(boolean buttonState, List<Action> actionList) {
            if (buttonState) {
                actionList.forEach(Action::invoke);
            }
        }
    }
}
