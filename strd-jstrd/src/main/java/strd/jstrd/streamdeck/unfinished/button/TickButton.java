package strd.jstrd.streamdeck.unfinished.button;

import strd.lib.iconpainter.IconPainter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;
import static strd.jstrd.streamdeck.unfinished.button.ButtonFactory.ButtonConfigurationDefinition.ButtonPropertyDataType.COLOR;

public class TickButton implements Button {

    private static final Logger log = getLogger(TickButton.class);

    private int tick = 0;
    private IconPainter iconPainter;

    @Override
    public void tick(Instant instant) {
        tick++;
    }

    @Override
    public byte[] draw() {
        log.debug("Drawing tick \"{}\"", tick);
        iconPainter.fillWholeIcon(0,0,0);
        iconPainter.setColor(255,255,255);

        iconPainter.writeTextCentered(""+tick);
        return iconPainter.toDeviceNativeFormat();
    }

    @Override
    public void preload(Supplier<IconPainter> iconPainterSupplier) {
        iconPainter = iconPainterSupplier.get();
    }

    @Override
    public boolean needsUpdate() {
        return true;
    }

    @Override
    public String toString() {
        return "Clock button";
    }

    @Override
    public void close() {
        iconPainter.close();
    }

    public static class Factory extends AbstractButtonFactory {
        public Factory() {
            super("tick", TickButton::new);
        }
    }
}
