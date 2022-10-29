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
import java.util.Map;
import java.util.function.Supplier;

public class ColorButton implements Button {

    private final int red;
    private final int green;
    private final int blue;
    private byte[] iconBytes;

    private final ButtonBehavior bb = new ButtonBehavior();
    //TODO MMUCHA: read from configuration
    private final List<Action> actionList;

    public ColorButton() {
        this(0, 0, 0, Collections.emptyList());
    }

    public ColorButton(int red, int green, int blue, List<Action> actionList) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.actionList = actionList;
    }

    public ColorButton(Color color, List<Action> actionList) {
        this(color.getRed(), color.getGreen(), color.getBlue(), actionList);
    }

    public static ColorButton create(StreamDeckConfiguration.ButtonConfiguration buttonConfiguration) {
        //TODO MMUCHA: make dynamic!
        StreamDeckConfiguration.ConditionalButtonConfiguration staticConfig =
                buttonConfiguration.findApplicableConditionalButtonConfiguration()
                        .orElseThrow(() -> new CannotHappenException("Should be protected by validation"));

        Color color =
                PropertiesUtil.getColorProperty(staticConfig.getProperties(), ColorButtonFactory.COLOR_PROPERTY_NAME);
        ActionConfiguration actionConfiguration = staticConfig.getActionConfiguration();


        throw new UnsupportedOperationException("Not implemented yet");
        return new ColorButton(color, actionList);
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
