package strd.jstrd.streamdeck.unfinished.action;

public class QuitActionFactory extends  SimpleActionFactory {
    public QuitActionFactory() {
        super("quit", QuitAction::new);
    }
}
