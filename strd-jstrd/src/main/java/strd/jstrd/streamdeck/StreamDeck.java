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
import strd.lib.spi.hid.HidLibrary;
import strd.lib.streamdeck.StreamDeckDevice;

import java.time.Instant;
import java.util.Arrays;
import java.util.function.Supplier;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class StreamDeck {

    private static final Logger log = getLogger(StreamDeck.class);
    //TODO MMUCHA: improve, why should we have supplier if we have factory already!
    private final Supplier<IconPainter> iconPainterSupplier;
    private final ButtonStateUpdaterListener buttonsStateUpdatedListener;

    private StreamDeckConfiguration.DeviceConfiguration configuration;
    private final StreamDeckDevice streamDeckDevice;
    private Disposable tickingFluxDisposable;

    private final StreamDeckButtonSetImpl shownButtons;

    private final Button blankButton;
    private ButtonContainer rootButtonContainer;

    public StreamDeck(StreamDeckDevice streamDeckDevice, IconPainterFactory iconPainterFactory) {
        this.streamDeckDevice = streamDeckDevice;
        int keyCount = streamDeckDevice.getStreamDeckInfo().getStreamDeckVariant().getKeyCount();
        shownButtons = new StreamDeckButtonSetImpl(keyCount);

        iconPainterSupplier = ()->iconPainterFactory.create(streamDeckDevice);

        blankButton = new ColorButton();
        buttonsStateUpdatedListener = new ButtonStateUpdaterListener();
    }

    //TODO MMUCHA: method start.
    public StreamDeck setConfiguration(StreamDeckConfiguration.DeviceConfiguration configuration) {
        //if this streamdeck had configuration already set, we need to stop it for a while.
        if (isRunning()) {
            throw new IllegalStateException("Cannot reset configuration, streamdeck currently running");
        }

        this.configuration = configuration;

        //TODO MMUCHA: read from configuration!
        rootButtonContainer = new SimpleButtonContainer(Arrays.asList(
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


        return this;
    }

    public StreamDeck start() {
        updateStreamDeckButtons();
        streamDeckDevice.addButtonsStateUpdatedListener(buttonsStateUpdatedListener);
        
        tickingFluxDisposable = Flux.interval(configuration.getUpdateInterval())
                .subscribe(e -> updateStreamDeckButtons(),
                ex -> {
                    //somehow unregister the device.
                    //TODO MMUCHA: can we somehow throw exception?
                    log.error("Updating streamdeck thrown exception, halting update process", ex);
                    CliUtil.printException(ex);
                });
        return this;
    }

    private void updateStreamDeckButtons() {
        log.debug("Regular update of stream deck {}",
                this.streamDeckDevice.getStreamDeckInfo().getSerialNumberString());
        Instant now = Instant.now();

        log.debug("Calling tick");
        rootButtonContainer.tick(now);
        log.debug("Calling tick done.");

        log.debug("Calculating buttons to show");
        rootButtonContainer.update(shownButtons);
        log.debug("Calculating buttons to show [done]");

        log.debug("updating buttons.");
        Flux.range(0, shownButtons.getButtonCount())
                .publishOn(Schedulers.parallel())
                .filter(shownButtons::buttonNeedsUpdate)
                .subscribe(index -> {
                    log.debug("Have to update button {}", index);
                    Button button = shownButtons.get(index);
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
                    shownButtons.flip();
                    log.debug("updating buttons [done]");
                });
    }

    public StreamDeck stop() {
        if (isRunning()) {
            //TODO MMUCHA: release all buttons!!
            tickingFluxDisposable.dispose();
            streamDeckDevice.removeButtonsStateUpdatedListener(buttonsStateUpdatedListener);
            tickingFluxDisposable = null;
        }
        return this;
    }

    public boolean isRunning() {
        return tickingFluxDisposable != null;
    }

    public void closeDevice() {
        stop();
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

    private class ButtonStateUpdaterListener extends StreamDeckDevice.ButtonStateListener.Adapter {
        @Override
        public void buttonStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo,
                                       int buttonIndex,
                                       boolean buttonState) {
            shownButtons.get(buttonIndex).updateButtonState(buttonState);
        }
    }
}
