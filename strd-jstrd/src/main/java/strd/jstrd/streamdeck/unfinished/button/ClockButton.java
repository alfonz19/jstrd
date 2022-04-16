package strd.jstrd.streamdeck.unfinished.button;

import strd.lib.iconpainter.IconPainter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ClockButton implements Button {


    private String timeString;
    private boolean needsUpdate;

    @Override
    public void tick(Instant instant) {
        String oldTimeString = timeString;
        timeString = LocalTime.ofInstant(instant, ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_TIME);
        needsUpdate = !oldTimeString.equals(timeString);
    }

    @Override
    public void draw() {
        needsUpdate = false;
        throw new UnsupportedOperationException("Not implemented yet");//TODO MMUCHA: implement!!
    }

    @Override
    public void preload(IconPainter iconPainter) {
        //do nothing.
    }

    @Override
    public boolean needsUpdate() {
        return needsUpdate;
    }
}
