package strd.jstrd.streamdeck;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.util.CustomCollectors;
import strd.jstrd.util.JacksonUtil;
import strd.lib.spi.hid.HidLibrary;
import strd.lib.streamdeck.StreamDeckManager;
import strd.lib.util.WaitUntilNotTerminated;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Daemon {
    private static final Logger log = getLogger(Daemon.class);

    private final WaitUntilNotTerminated wunt = new WaitUntilNotTerminated();
    private StreamDeckConfiguration configuration = new StreamDeckConfiguration();
    private final StreamDeckManager streamDeckManager;
    private final Map<Object, StreamDeck> registeredDevices = new HashMap<>();

    public Daemon(HidLibrary library, boolean withoutKeyHook) {
        streamDeckManager = new StreamDeckManager(library);
        log.debug("Creating with configuration {} and keybordHook={}",
                JacksonUtil.serializeAsString(configuration),
                withoutKeyHook);

    }

    public void start() {
        DaemonDeviceListener listener = new DaemonDeviceListener();

        //will handle only new devices;
        streamDeckManager.addListener(listener);
        //register already plugged in devices.
        streamDeckManager.findStreamDeckDevices().forEach(listener::deviceAdded);

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
            StreamDeck streamDeckSomething = registeredDevices.get(deviceConfig.getSerialNumber());
            //and if we have one, update its configuration.
            if (streamDeckSomething != null) {
                streamDeckSomething.setConfiguration(deviceConfig);
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

    private synchronized void registerDevice(HidLibrary.StreamDeckInfo streamDeckInfo) {
        getConfigurationForDevice(streamDeckInfo)
                .ifPresent(configuration -> registeredDevices.put(
                        streamDeckInfo.getSerialNumberString(),
                        new StreamDeck(streamDeckManager.openConnection(streamDeckInfo)).setConfiguration(configuration)));
    }

    private synchronized void unregisterDevice(HidLibrary.StreamDeckInfo streamDeckInfo) {
        registeredDevices.remove(streamDeckInfo.getSerialNumberString()).closeDevice();
    }

//    //TODO MMUCHA: synchronize access to this class.
//    private void unregisterAllDevices() {
//        registeredDevices.values().forEach(StreamDeckSomething::closeDevice);
//        registeredDevices.clear();
//    }

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
            unregisterDevice(streamDeckInfo);
        }
    }
}
