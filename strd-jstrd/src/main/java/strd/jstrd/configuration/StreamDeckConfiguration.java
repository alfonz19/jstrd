package strd.jstrd.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
        public static final String LAYOUT_MUST_BE_SPECIFIED = "Layout must be specified";
        public static final String SERIAL_VERSION_MUST_BE_SET = "serial version must be set";

        @NotEmpty(message = SERIAL_VERSION_MUST_BE_SET)
        private String serialNumber;

        private Duration updateInterval = ChronoUnit.FOREVER.getDuration();

        @Valid
        @NotNull(message = LAYOUT_MUST_BE_SPECIFIED)
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
        @Valid
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
        @NotEmpty
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

