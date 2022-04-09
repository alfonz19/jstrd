package strd.jstrd.streamdeck.unfinished;

/**
 * Action to be performed when X.
 */
public interface Action {
    String getActionName();

    void invokeAction(ButtonContext buttonContext);
}
