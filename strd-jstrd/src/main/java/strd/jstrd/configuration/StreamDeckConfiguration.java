package strd.jstrd.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamDeckConfiguration {

    @Valid
    private List<DeviceConfiguration> devices = new ArrayList<>();

    public List<DeviceConfiguration> getDevices() {
        return devices;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeviceConfiguration {
        private String serialNumber;

        private Duration updateInterval = ChronoUnit.FOREVER.getDuration();

        @Valid
        public LeafOrNonLeaf layout;

        public String getSerialNumber() {
            return serialNumber;
        }

        public Duration getUpdateInterval() {
            return updateInterval;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @LeafOrNonLeafValidation
    public static class LeafOrNonLeaf {
        @Valid
        public List<NonLeaf> containers;
        public List<ButtonConfiguration> buttons;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NonLeaf extends LeafOrNonLeaf {
        public String type;
        public Map<String, Object> properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ButtonConfiguration {
        public String type;
        public Map<String, Object> properties;
//        public String index;
//        public String name;
//        public String description;
//        public String buttonType;
//        public Map<String, String> configuration;
    }
}

