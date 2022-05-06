package strd.jstrd.streamdeck.unfinished.button;

public class ClockButtonFactory extends SimpleButtonFactory {
    public ClockButtonFactory() {
        super("clock", ClockButton::new);
    }
}
