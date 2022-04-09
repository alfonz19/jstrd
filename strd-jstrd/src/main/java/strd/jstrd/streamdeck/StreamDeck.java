package strd.jstrd.streamdeck;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.streamdeck.unfinished.Button;
import strd.jstrd.streamdeck.unfinished.StreamDeckButtonSet;
import strd.lib.streamdeck.StreamDeckDevice;

import java.util.ArrayList;
import java.util.List;

public class StreamDeck {

    private StreamDeckConfiguration.DeviceConfiguration configuration;
    private final StreamDeckDevice streamDeck;

    public StreamDeck(StreamDeckDevice streamDeckDevice) {
        this.streamDeck = streamDeckDevice;
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public StreamDeck setConfiguration(StreamDeckConfiguration.DeviceConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public void closeDevice() {
        streamDeck.close();
    }


    private static final class StreamDeckButtonSetImpl implements StreamDeckButtonSet {
        private boolean locked = false;
        private final List<Button> buttons;

        public StreamDeckButtonSetImpl(int numberOfButtons) {
            buttons = new ArrayList<>(numberOfButtons);
        }

        @Override
        public boolean isAvailable(int index) {
            return buttons.get(index) == null;
        }

        @Override
        public Button get(int index) {
            return buttons.get(index);
        }

        @Override
        public void set(int index, Button button) {
            if (locked) {
                throw new IllegalStateException("Buttons set is locked, cannot set buttons.");
            }

            if (!isAvailable(index)) {
                throw new IllegalStateException(String.format("Button at position %d was already set", index));
            }

            buttons.set(index, button);
        }

        public void lock() {
            this.locked = true;
        }

        public void clear() {
            this.locked = false;
            this.buttons.clear();
        }
    }
}
