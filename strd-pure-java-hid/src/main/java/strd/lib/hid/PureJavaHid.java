package strd.lib.hid;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import strd.lib.common.Constants;
import strd.lib.common.exception.StrdException;
import strd.lib.spi.hid.HidLibrary;
import strd.lib.spi.hid.StreamDeckHandle;
import strd.lib.spi.hid.StreamDeckVariant;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PureJavaHid implements HidLibrary {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PureJavaHid.class);

    private final List<HidLibrary.DeviceListener> listeners = new ArrayList<>();
    private Map<String, HidLibrary.StreamDeckInfo> existingDevices;
    private Duration pollingDuration = Duration.ofSeconds(1);
    private Disposable deviceDetectionFlux = null;

    @Override
    public List<StreamDeckInfo> findStreamDeckDevices() {
        return purejavahidapi.PureJavaHidApi.enumerateDevices()
                .stream()
                .map(info -> {
                    short vendorId = info.getVendorId();
                    short productId = info.getProductId();
                    String productString = info.getProductString();
                    String serialNumberString = info.getSerialNumberString();
                    return StreamDeckVariant.valueOf(vendorId, productId)
                            .map(streamDeckVariant -> new StreamDeckInfoImpl(streamDeckVariant, info));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public StreamDeckHandle createStreamDeckHandle(StreamDeckInfo streamDeckInfo) {
        try {
            HidDeviceInfo internalRepresentation = ((StreamDeckInfoImpl) streamDeckInfo).getInternalRepresentation();
            HidDevice openedDevice = purejavahidapi.PureJavaHidApi.openDevice(internalRepresentation);
            PureJavaHidApiStreamDeckHandle result = new PureJavaHidApiStreamDeckHandle(openedDevice, streamDeckInfo);
            result.setDeviceRemovalListener(e -> {/*do nothing*/});
            return result;
        } catch (IOException e) {
            throw new StrdException(e);
        }
    }

    @Override
    public void addListener(HidLibrary.DeviceListener listener) {
        if (!isDeviceDetectionRunning()) {
            existingDevices = findStreamDeckDevices().stream()
                    .collect(Collectors.toMap(this::getDeviceNaturalId, Function.identity()));
            deviceDetectionFlux = Flux.interval(pollingDuration).subscribe(e -> detectDeviceAddRemoveAndNotify(), e->{
                //TODO MMUCHA: can we somehow throw exception?
                log.error("Unexpected exception", e);
            });
        }
        listeners.add(listener);
    }

    @Override
    public void removeListener(HidLibrary.DeviceListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            removeListeners();
        }
    }

    @Override
    public void removeListeners() {
        if (isDeviceDetectionRunning()) {
            deviceDetectionFlux.dispose();
            deviceDetectionFlux = null;
            existingDevices = null;
        }
        listeners.clear();
    }

    @Override
    public void setPollingInterval(Duration duration) {
        long durationAsMillis = duration.toMillis();
        if (durationAsMillis < Constants.FASTEST_REFRESH_INTERVAL_MILLIS) {
            duration = Duration.ofMillis(Constants.FASTEST_REFRESH_INTERVAL_MILLIS);
            log.warn("Too quick refresh interval, resetting to {}", duration);
        } else if (durationAsMillis > Constants.SLOWEST_REFRESH_INTERVAL_MILLIS) {
            duration = Duration.ofMillis(Constants.SLOWEST_REFRESH_INTERVAL_MILLIS);
            log.warn("Too slow refresh interval, resetting to {}", duration);
        }

        this.pollingDuration = duration;
    }

    private boolean isDeviceDetectionRunning() {
        return deviceDetectionFlux != null;
    }

    private void detectDeviceAddRemoveAndNotify() {
        List<HidLibrary.StreamDeckInfo> foundDevices = findStreamDeckDevices();
        foundDevices.stream()
                .filter(info -> !existingDevices.containsKey(getDeviceNaturalId(info)))
                .forEach(info -> {
                    listeners.forEach(listener -> listener.deviceAdded(info));
                    existingDevices.put(getDeviceNaturalId(info), info);
                });

        for (Iterator<Map.Entry<String, StreamDeckInfo>> iterator = existingDevices.entrySet().iterator();
             iterator.hasNext(); ) {
            Map.Entry<String, HidLibrary.StreamDeckInfo> entry = iterator.next();
            String naturalId = entry.getKey();
            boolean deleted = foundDevices.stream().map(this::getDeviceNaturalId).noneMatch(e -> e.equals(naturalId));
            if (deleted) {
                iterator.remove();
                listeners.forEach(listener -> listener.deviceRemoved(entry.getValue()));
            }
        }
    }

    private String getDeviceNaturalId(HidLibrary.StreamDeckInfo e) {
        return e.getSerialNumberString() + ":::" + e.getProductString();
    }

    private static class PureJavaHidApiStreamDeckHandle implements StreamDeckHandle {

        private HidDevice hidDevice;
        private final StreamDeckInfo streamDeckInfo;

        public PureJavaHidApiStreamDeckHandle(HidDevice hidDevice, StreamDeckInfo streamDeckInfo) {
            this.hidDevice = hidDevice;
            this.streamDeckInfo = streamDeckInfo;
        }

        @Override
        public void close() {
            assertOpenedDevice();
            hidDevice.close();
            hidDevice = null;
        }

        @Override
        public void setInputReportListener(InputReportListener inputReportListener) {
            assertOpenedDevice();
            hidDevice.setInputReportListener((source, reportID, reportData, reportLength) ->
                    inputReportListener.onInputReport(reportData, reportLength));
        }

        @Override
        public void setDeviceRemovalListener(DeviceRemovalListener deviceRemovalListener) {
            assertOpenedDevice();
            hidDevice.setDeviceRemovalListener(source -> {
                //regardless if user want's or not, this device was removed, so we call close to have up-to-date info, that
                //this device is closed.
                close();
                deviceRemovalListener.onDeviceRemoved(streamDeckInfo);
            });
        }

        private void assertOpenedDevice() {
            if (isClosed()) {
                throw new StrdException("Device already closed");
            }
        }

        @Override
        public boolean isClosed() {
            return hidDevice == null;
        }

        @Override
        public int getFeatureReport(byte[] requestBytes, int length) {
            return hidDevice.getFeatureReport(requestBytes, length);
        }

        @Override
        public int setFeatureReport(byte reportId, byte[] data, int length) {
            return hidDevice.setFeatureReport(reportId, data, length);
        }

        @Override
        public int setOutputReport(byte reportId, byte[] data, int length) {
            return hidDevice.setOutputReport(reportId, data, length);
        }

        @Override
        public StreamDeckInfo getStreamDeckInfo() {
            return streamDeckInfo;
        }
    }

    public static class StreamDeckInfoImpl implements StreamDeckInfo {
        private final short vendorId;
        private final short productId;
        private final StreamDeckVariant streamDeckVariant;
        private final String serialNumberString;
        private final String productString;
        private final HidDeviceInfo internalRepresentation;

        public StreamDeckInfoImpl(StreamDeckVariant streamDeckVariant, HidDeviceInfo hidDeviceInfo) {
            this.streamDeckVariant = streamDeckVariant;
            this.vendorId = hidDeviceInfo.getVendorId();
            this.productId = hidDeviceInfo.getProductId();
            this.serialNumberString = hidDeviceInfo.getSerialNumberString();
            this.productString = hidDeviceInfo.getProductString();
            this.internalRepresentation = hidDeviceInfo;
        }

        @Override
        public short getVendorId() {
            return vendorId;
        }

        @Override
        public short getProductId() {
            return productId;
        }

        @Override
        public StreamDeckVariant getStreamDeckVariant() {
            return streamDeckVariant;
        }

        public HidDeviceInfo getInternalRepresentation() {
            return internalRepresentation;
        }

        @Override
        public String getSerialNumberString() {
            return serialNumberString;
        }

        @Override
        public String getProductString() {
            return productString;
        }

        @Override
        public String toString() {
            return "StreamDeckInfo {\n" + "\tvendorId=" + vendorId + ",\n" +
                    "\tproductId=" + productId + ",\n" +
                    "\tstreamDeckVariant=" + streamDeckVariant + ",\n" +
                    "\tserialNumberString='" + serialNumberString + '\'' + ",\n" +
                    "\tproductString='" + productString + '\'' + "\n" +
                    '}';
        }
    }
}
