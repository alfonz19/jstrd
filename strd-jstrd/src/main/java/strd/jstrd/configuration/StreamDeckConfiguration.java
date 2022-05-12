package strd.jstrd.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public class StreamDeckConfiguration {

    @Valid
    private final List<DeviceConfiguration> devices = new ArrayList<>();

    public List<DeviceConfiguration> getDevices() {
        return devices;
    }

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

        public DeviceConfiguration setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public DeviceConfiguration setUpdateInterval(Duration updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }

        public DeviceConfiguration setLayout(ContainerConfiguration layout) {
            this.layout = layout;
            return this;
        }
    }

    @Getter
    @Setter
    @ContainerValidation
    public static class ContainerConfiguration {
        @NotEmpty
        private String type;
        private String name;
        private String description;
        @Valid
        public List<ContainerConfiguration> containers;
        @Valid
        public List<ButtonConfiguration> buttons;
        private Map<String, Object> properties;

        public boolean isLeafContainer() {
            return getContainers() == null;
        }
    }

    @Getter
    @Setter
    public static class ButtonConfiguration {

        @NotEmpty
        private String type;
        private String name;
        private String description;

        @JsonProperty("configurations")
        private final List<ConditionalButtonConfiguration> conditionalConfigurations = new ArrayList<>();

        public Optional<ConditionalButtonConfiguration> findApplicableConditionalButtonConfiguration() {
            int numberOfConfigurations = conditionalConfigurations.size();
            if (numberOfConfigurations == 0) {
                return Optional.empty();
            }

            return Optional.of(conditionalConfigurations.stream()
                    .filter(e ->/*evaluateEl()*/false)    //TODO MMUCHA: implement!
                    .findFirst()
                    .orElseGet(() -> conditionalConfigurations.get(numberOfConfigurations - 1)));
        }

//        public String index;
//        public String buttonType;
//        public Map<String, String> configuration;
    }

    @Getter
    @Setter
    public static class ConditionalButtonConfiguration {
        private String el;
        private Map<String, Object> properties;
        @JsonProperty("action")
        @Valid
        private ActionConfiguration actionConfiguration;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class ActionConfiguration {
        @NotEmpty
        private String type;
//        private List<String> boundEvents;//TODO MMUCHA: implement.
        private Map<String, Object> properties;

        public ActionConfiguration(String type) {
            this(type, null);
        }

        public ActionConfiguration(String type, Map<String, Object> properties) {
            this.type = type;
            this.properties = properties;
        }
    }
}

