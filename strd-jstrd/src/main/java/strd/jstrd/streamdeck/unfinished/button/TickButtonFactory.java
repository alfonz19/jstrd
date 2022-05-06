package strd.jstrd.streamdeck.unfinished.button;

public class TickButtonFactory extends SimpleButtonFactory {
    public TickButtonFactory() {
        super("tick", TickButton::new);
    }
}
