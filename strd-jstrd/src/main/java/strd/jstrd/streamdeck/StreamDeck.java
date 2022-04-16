package strd.jstrd.streamdeck;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.streamdeck.unfinished.StreamDeckButtonSet;
import strd.jstrd.streamdeck.unfinished.button.Button;
import strd.jstrd.streamdeck.unfinished.button.ClockButton;
import strd.jstrd.streamdeck.unfinished.button.ColorButton;
import strd.jstrd.streamdeck.unfinished.button.TickButton;
import strd.jstrd.streamdeck.unfinished.buttoncontainer.ButtonContainer;
import strd.jstrd.streamdeck.unfinished.buttoncontainer.SimpleButtonContainer;
import strd.jstrd.util.CliUtil;
import strd.lib.iconpainter.IconPainter;
import strd.lib.iconpainter.factory.IconPainterFactory;
import strd.lib.streamdeck.StreamDeckDevice;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.function.Supplier;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class StreamDeck {

    private static final Logger log = getLogger(StreamDeck.class);
    //TODO MMUCHA: improve, why should we have supplier if we have factory already!
    private final Supplier<IconPainter> iconPainterSupplier;

    private StreamDeckConfiguration.DeviceConfiguration configuration;
    private final StreamDeckDevice streamDeckDevice;
    private Disposable tickingFluxDisposable;

    private final int keyCount;

    private StreamDeckButtonSetImpl streamDeckButtonSet;

    private final Button blankButton;

    public StreamDeck(StreamDeckDevice streamDeckDevice, IconPainterFactory iconPainterFactory) {
        this.streamDeckDevice = streamDeckDevice;
        keyCount = streamDeckDevice.getStreamDeckInfo().getStreamDeckVariant().getKeyCount();
        streamDeckButtonSet = new StreamDeckButtonSetImpl(keyCount);

        iconPainterSupplier = ()->iconPainterFactory.create(streamDeckDevice);

        blankButton = new ColorButton();
    }

    //TODO MMUCHA: method start.
    public StreamDeck setConfiguration(StreamDeckConfiguration.DeviceConfiguration configuration) {
        //if this streamdeck had configuration already set, we need to stop it for a while.
        if (tickingFluxDisposable != null) {
            resetConfiguration();

        }

        this.configuration = configuration;
        Duration updateInterval = configuration.getUpdateInterval();

        //TODO MMUCHA: read from configuration!
        ButtonContainer rootButtonContainer = new SimpleButtonContainer(Arrays.asList(
                new ClockButton(),
                new TickButton(),
                new ColorButton(255,0,0),
                new ColorButton(0, 255,0),
                new ColorButton(0, 0, 255)
        ));

        log.debug("Preloading all buttons for configuration");
        rootButtonContainer.preload(iconPainterSupplier);
        //preloading blank button.
        blankButton.preload(iconPainterSupplier);
        log.debug("Preloading done");


        tickingFluxDisposable = Flux.interval(updateInterval).subscribe(e -> {
                    log.debug("Regular update of stream deck {}",
                            this.streamDeckDevice.getStreamDeckInfo().getSerialNumberString());
                    Instant now = Instant.now();

                    log.debug("Calling tick");
                    rootButtonContainer.tick(now);
                    log.debug("Calling tick done.");

                    log.debug("Calculating buttons to show");
                    rootButtonContainer.update(streamDeckButtonSet);
                    log.debug("Calculating buttons to show [done]");

                    log.debug("updating buttons.");
                    Flux.range(0, streamDeckButtonSet.getButtonCount())
                            .publishOn(Schedulers.parallel())
                            .filter(index -> streamDeckButtonSet.buttonNeedsUpdate(index))
                            .subscribe(index -> {
                                log.debug("Have to update button {}", index);
                                Button button = streamDeckButtonSet.get(index);
                                if (button == null) {
                                    log.debug("Replacing null-value button with blank button.");
                                    button = blankButton;
                                }

                                streamDeckDevice.setButtonImage(index, button.draw());
                            }, ex -> {
                                //this was caused by single buttong update, not whole device; the whole device is below.
                                //TODO MMUCHA: cli error, unregister device.
                                log.error("Updating streamdeck thrown exception, halting update process", ex);
                            }, () -> {

                                //prepare instance for another round.
                                streamDeckButtonSet.flip();
                                log.debug("updating buttons [done]");
                            });

                },
                ex -> {
//            somehow unregister the device.
                    //TODO MMUCHA: can we somehow throw exception?
                    log.error("Updating streamdeck thrown exception, halting update process", ex);
                    CliUtil.printException(ex);
                });

        return this;
    }

    private void resetConfiguration() {
        //TODO MMUCHA: release all buttons!!
        tickingFluxDisposable.dispose();
        tickingFluxDisposable = null;
    }

    public void closeDevice() {
        resetConfiguration();
        streamDeckDevice.close();
    }

    private static final class StreamDeckButtonSetImpl implements StreamDeckButtonSet {
        private final int buttonCount;

        private boolean firstUpdate = true;
        private Button[] oldButtons;
        private Button[] buttons;

        public StreamDeckButtonSetImpl(int buttonCount) {
            this.buttonCount = buttonCount;
            if (buttonCount <= 0) {
                throw new IllegalArgumentException();
            }

            buttons = new Button[buttonCount];
            oldButtons = new Button[buttonCount];
        }

        @Override
        public int getButtonCount() {
            return buttonCount;
        }

        @Override
        public boolean isAvailable(int index) {
            return buttons[index] == null;
        }

        @Override
        public void set(int index, Button button) {
            if (!isAvailable(index)) {
                throw new IllegalStateException(String.format("Button at position %d was already set", index));
            }

            buttons[index] = button;
        }

        public boolean buttonNeedsUpdate(int index) {
            //we just started.
            if (firstUpdate) {
                return true;
            }

            Button newButton = buttons[index];
            Button oldButton = oldButtons[index];

            if (oldButton == newButton) {
                return newButton != null && newButton.needsUpdate();
            } else {
                return true;
            }
        }

        public Button get(int index) {
            return buttons[index];
        }

        public void flip() {
            firstUpdate = false;

            Button[] tmp = oldButtons;
            oldButtons = buttons;
            buttons = tmp;
            Arrays.fill(buttons, null);
        }
    }
}
