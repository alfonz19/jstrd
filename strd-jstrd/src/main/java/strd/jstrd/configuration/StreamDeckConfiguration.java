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
        private StreamDeckConfiguration.ContainerConfiguration layout;

        public String getSerialNumber() {
            return serialNumber;
        }

        public Duration getUpdateInterval() {
            return updateInterval;
        }

        public ContainerConfiguration getLayout() {
            return layout;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @ContainerValidation
    public static class ContainerConfiguration extends CommonConfiguration {
        @Valid
        public List<ContainerConfiguration> containers;
        public List<ButtonConfiguration> buttons;

        public List<ContainerConfiguration> getContainers() {
            return containers;
        }

        public List<ButtonConfiguration> getButtons() {
            return buttons;
        }

        public boolean isLeafContainer() {
            return getContainers() == null;
        }
    }

//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class ContainerConfiguration extends Container {
//
//    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonConfiguration extends CommonConfiguration {
//        public String type;
//        public Map<String, Object> properties;
//        public String index;
//        public String name;
//        public String description;
//        public String buttonType;
//        public Map<String, String> configuration;


//        public String getType() {
//            return type;
//        }
//
//        public Map<String, Object> getProperties() {
//            return properties;
//        }
    }

    public static class CommonConfiguration {
        private String type;
        private Map<String, Object> properties;

        public String getType() {
            return type;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }
    }
}

