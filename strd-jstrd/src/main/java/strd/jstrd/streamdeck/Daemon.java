package strd.jstrd.streamdeck;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.util.CustomCollectors;
import strd.jstrd.util.JacksonUtil;
import strd.lib.iconpainter.factory.IconPainterFactory;
import strd.lib.spi.hid.HidLibrary;
import strd.lib.streamdeck.StreamDeckDevice;
import strd.lib.streamdeck.StreamDeckManager;
import strd.lib.util.WaitUntilNotTerminated;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

//TODO MMUCHA: synchronize access to this class.
public class Daemon {
    private static final Logger log = getLogger(Daemon.class);

    private final WaitUntilNotTerminated wunt = new WaitUntilNotTerminated(1000);
    private StreamDeckConfiguration configuration = new StreamDeckConfiguration();
    private final StreamDeckManager streamDeckManager;
    private final IconPainterFactory iconPainterFactory;
    private final Map<Object, StreamDeck> registeredDevices = new HashMap<>();

    public Daemon(HidLibrary library,
                  IconPainterFactory iconPainterFactory,
                  boolean withoutKeyHook) {
        streamDeckManager = new StreamDeckManager(library);
        this.iconPainterFactory = iconPainterFactory;
        log.debug("Creating with configuration {} and keybordHook={}",
                JacksonUtil.serializeAsString(configuration),
                withoutKeyHook);

    }

    public void start() {
        DaemonDeviceListener listener = new DaemonDeviceListener();

        //will handle only new devices
        streamDeckManager.addListener(listener);
        //register already plugged in devices.
        streamDeckManager.findStreamDeckDevices().forEach(listener::deviceAdded);

        //just spin wait on main thread to avoid termination.
        wunt.start();
    }

    public void stop() {
        wunt.terminate();
    }

    public void setConfiguration(StreamDeckConfiguration configuration) {
        //store whole configuration
        this.configuration = configuration;

//        go over each device configuration in configuration instance
        configuration.getDevices().forEach(deviceConfig -> {
            //look for registered device using that configuration
            StreamDeck streamDeck = registeredDevices.get(deviceConfig.getSerialNumber());
            //and if we have one, update its configuration.
            if (streamDeck != null) {
                streamDeck.stop().setConfiguration(deviceConfig).start();
            }
        });
    }

    private Optional<StreamDeckConfiguration.DeviceConfiguration> getConfigurationForDevice(HidLibrary.StreamDeckInfo streamDeckInfo) {
        Stream<StreamDeckConfiguration.DeviceConfiguration> stream = this.configuration.getDevices().stream();
        String deviceSerialNumber = streamDeckInfo.getSerialNumberString();

        return stream
                .filter(e -> e.getSerialNumber().equals(deviceSerialNumber))
                .collect(CustomCollectors.atMostOneRecordCollector());
    }

    public synchronized void registerDevice(HidLibrary.StreamDeckInfo streamDeckInfo) {
        getConfigurationForDevice(streamDeckInfo)
                .ifPresent(deviceConfiguration -> registeredDevices.put(
                        streamDeckInfo.getSerialNumberString(),
                        createStreamDeck(streamDeckInfo).setConfiguration(deviceConfiguration).start()));
    }

    private StreamDeck createStreamDeck(HidLibrary.StreamDeckInfo streamDeckInfo) {
        StreamDeckDevice streamDeckDevice = streamDeckManager.openConnection(streamDeckInfo);
        return new StreamDeck(streamDeckDevice, iconPainterFactory);
    }

    /**
     *
     * @param streamDeckInfo details about streamdeck
     * @param makeAttemptToCloseDevice it's possible, that we're notified about device removal and based on that we called
     *                                 its unregistering. In that case, trying to close the device will yield error,
     *                                 as the device is gone already.
     */
    public synchronized void unregisterDevice(HidLibrary.StreamDeckInfo streamDeckInfo, boolean makeAttemptToCloseDevice) {
        StreamDeck registeredDevice = registeredDevices.remove(streamDeckInfo.getSerialNumberString());
        if (registeredDevice != null) {
            registeredDevice.stop();
            if (makeAttemptToCloseDevice) {
                registeredDevice.closeDevice();
            }
        }
    }

    public StreamDeckConfiguration getConfiguration() {
        return configuration;
    }

    private class DaemonDeviceListener implements HidLibrary.DeviceListener {

        @Override
        public void deviceAdded(HidLibrary.StreamDeckInfo streamDeckInfo) {
            log.debug("Plugged-in device {}: {}", streamDeckInfo.getSerialNumberString(), streamDeckInfo.getProductString());
            registerDevice(streamDeckInfo);

        }

        @Override
        public void deviceRemoved(HidLibrary.StreamDeckInfo streamDeckInfo) {
            log.debug("Unplugged device {}: {}", streamDeckInfo.getSerialNumberString(), streamDeckInfo.getProductString());
            unregisterDevice(streamDeckInfo, false);
        }
    }
}
