package strd.jstrd.streamdeck;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.streamdeck.unfinished.button.Button;
import strd.jstrd.streamdeck.unfinished.StreamDeckButtonSet;
import strd.jstrd.streamdeck.unfinished.button.ClockButton;
import strd.jstrd.streamdeck.unfinished.button.ColorButton;
import strd.jstrd.streamdeck.unfinished.buttoncontainer.ButtonContainer;
import strd.jstrd.streamdeck.unfinished.buttoncontainer.SimpleButtonContainer;
import strd.lib.iconpainter.IconPainter;
import strd.lib.streamdeck.StreamDeckDevice;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class StreamDeck {

    private StreamDeckConfiguration.DeviceConfiguration configuration;
    private final StreamDeckDevice streamDeckDevice;
    private final IconPainter iconPainter = null; //TODO MMUCHA: !
    private Disposable tickingFluxDisposable;

    private final int keyCount;

    private StreamDeckButtonSetImpl oldSet;
    private StreamDeckButtonSetImpl newSet;

    private final Button blankButton;

    public StreamDeck(StreamDeckDevice streamDeckDevice) {
        this.streamDeckDevice = streamDeckDevice;
        keyCount = streamDeckDevice.getStreamDeckInfo().getStreamDeckVariant().getKeyCount();
        oldSet = new StreamDeckButtonSetImpl(keyCount);
        newSet = new StreamDeckButtonSetImpl(keyCount);

        blankButton = new ColorButton();
    }

    public StreamDeck setConfiguration(StreamDeckConfiguration.DeviceConfiguration configuration) {
        //if this streamdeck had configuration already set, we need to stop it for a while.
        if (tickingFluxDisposable != null) {
            resetConfiguration();

        }

        this.configuration = configuration;
        Duration updateInterval = configuration.getUpdateInterval();

        //TODO MMUCHA: read from configuration!
        ButtonContainer rootButtonContainer = new SimpleButtonContainer(Arrays.asList(
                new ClockButton()
        ));

        //call preload on root.
        rootButtonContainer.preload(iconPainter);
        blankButton.preload(iconPainter);

        tickingFluxDisposable = Flux.interval(updateInterval).subscribe(e -> {
            Instant now = Instant.now();
            oldSet = newSet;
            rootButtonContainer.tick(now);
            rootButtonContainer.update(newSet);

            Flux.range(0, newSet.getMaxIndex())
                    .publishOn(Schedulers.parallel())
                    .subscribe(index-> {
                        Button newButton = newSet.get(index);
                        Button oldButton = oldSet.get(index);

                        //button wasn't set.
                        if (newButton == null) {
                            draw blank.
                        } else if (newButton == oldButton) {
                            if (!newButton.needsUpdate()) {
                                //no change, do nothing.
                                return;
                            }
                        }

                        if (newButton == null) {

                        } else if ()
                    })


        });

        return this;
    }

    private void resetConfiguration() {
        //TODO MMUCHA: release all buttons!!
        tickingFluxDisposable.dispose();
        oldSet.clear();
        newSet.clear();
        tickingFluxDisposable = null;
    }

    public void closeDevice() {
        resetConfiguration();
        streamDeckDevice.close();
    }

    private static final class StreamDeckButtonSetImpl implements StreamDeckButtonSet {
        private boolean locked = false;
        private final List<Button> buttons;
        private final int maxIndex;

        public StreamDeckButtonSetImpl(int numberOfButtons) {
            buttons = new ArrayList<>(numberOfButtons);
            this.maxIndex = numberOfButtons - 1;
        }

        @Override
        public int getMaxIndex() {
            return maxIndex;
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
