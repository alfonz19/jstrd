package strd.jstrd.streamdeck.unfinished;

import strd.jstrd.streamdeck.unfinished.button.Button;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Representation of list of buttons, as they are currently is / previously were made visible. Allowing some 'locking'
 * behavior, so that tree structure can compete to set what buttons are visible, but not overwrite each other request.
 * This is not intended to be thread safe in first place, because tree structure has to be walked through
 * in predictable and stable order, but just to protect values already set from being overwritten and to allow
 * decisions, if 'someone' can set the button. For example, it wants to set all top row buttons or nothing.
 */
public interface StreamDeckButtonSet {
    int getMaxIndex();
    
    boolean isAvailable(int index);

    Button get(int index);

    void set(int index, Button button);

    default boolean isAvailable(int... index) {
        return Arrays.stream(index).boxed().map(this::isAvailable).reduce(true, (a, b)->a && b);
    }

    default void setIfAvailable(int index, Function<Integer, Button> getButtonFunction) {
        if (isAvailable(index)) {
            set(index, getButtonFunction.apply(index));
        }
    }

    default void setButtonsIfAvailable(Function<Integer, Button> get) {
        IntStream.rangeClosed(0, getMaxIndex()).forEach(index->{
            setIfAvailable(index, get);
        });
    }
}
