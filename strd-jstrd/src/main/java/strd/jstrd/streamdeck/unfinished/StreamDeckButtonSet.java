package strd.jstrd.streamdeck.unfinished;

import java.util.Arrays;

/**
 * Representation of list of buttons, as they are currently is / previously were made visible. Allowing some 'locking'
 * behavior, so that tree structure can compete to set what buttons are visible, but not overwrite each other request.
 * This is not intended to be thread safe in first place, because tree structure has to be walked through
 * in predictable and stable order, but just to protect values already set from being overwritten and to allow
 * decisions, if 'someone' can set the button. For example, it wants to set all top row buttons or nothing.
 */
public interface StreamDeckButtonSet {
    boolean isAvailable(int index);

    default boolean isAvailable(int... index) {
        return Arrays.stream(index).boxed().map(this::isAvailable).reduce(true, (a, b)->a && b);
    }

    Button get(int index);

    void set(int index, Button button);

}
