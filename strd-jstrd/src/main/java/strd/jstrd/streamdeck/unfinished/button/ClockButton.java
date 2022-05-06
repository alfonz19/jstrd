package strd.jstrd.streamdeck.unfinished.button;

import strd.lib.iconpainter.IconPainter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ClockButton implements Button {

    private static final Logger log = getLogger(ClockButton.class);

    private String timeString;
    private boolean needsUpdate;
    private IconPainter iconPainter;

    @Override
    public void tick(Instant instant) {
        String oldTimeString = timeString;
        timeString = LocalTime.ofInstant(instant, ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_TIME);
        needsUpdate = oldTimeString == null || !oldTimeString.equals(timeString);
        log.debug("Updating timestring to \"{}\"", timeString);
    }

    @Override
    public byte[] draw() {
        log.debug("Drawing \"{}\"", timeString);
        needsUpdate = false;
        iconPainter.fillWholeIcon(0,0,0);
        iconPainter.setColor(255,255,255);
        //TODO MMUCHA: not necessary to calculate size every time.
        iconPainter.writeTextCentered(timeString);
        return iconPainter.toDeviceNativeFormat();
    }

    @Override
    public void preload(Supplier<IconPainter> iconPainterSupplier) {
        iconPainter = iconPainterSupplier.get();
    }

    @Override
    public boolean needsUpdate() {
        return needsUpdate;
    }

    @Override
    public String toString() {
        return "Clock button";
    }

    @Override
    public void close() {
        this.iconPainter.close();
    }

    @Override
    public void updateButtonState(boolean buttonState) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
