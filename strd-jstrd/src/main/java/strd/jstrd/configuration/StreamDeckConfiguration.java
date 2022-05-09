package strd.jstrd.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    @ContainerValidation
    public static class ContainerConfiguration extends CommonConfiguration {
        @Valid
        public List<ContainerConfiguration> containers;
        @Valid
        public List<ButtonConfiguration> buttons;
        private Map<String, Object> properties;

        public boolean isLeafContainer() {
            return getContainers() == null;
        }

        public List<ContainerConfiguration> getContainers() {
            return containers;
        }

        public ContainerConfiguration setContainers(List<ContainerConfiguration> containers) {
            this.containers = containers;
            return this;
        }

        public List<ButtonConfiguration> getButtons() {
            return buttons;
        }

        public ContainerConfiguration setButtons(List<ButtonConfiguration> buttons) {
            this.buttons = buttons;
            return this;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public ContainerConfiguration setProperties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }
    }

    public static class ButtonConfiguration extends CommonConfiguration {

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

        public List<ConditionalButtonConfiguration> getConditionalConfigurations() {
            return conditionalConfigurations;
        }

//        public String index;
//        public String buttonType;
//        public Map<String, String> configuration;
    }

    public static class ConditionalButtonConfiguration {
        private String el;
        private Map<String, Object> properties;
        @JsonProperty("action")
        @Valid
        private ActionConfiguration actionConfiguration;

        public String getEl() {
            return el;
        }

        public ConditionalButtonConfiguration setEl(String el) {
            this.el = el;
            return this;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public ConditionalButtonConfiguration setProperties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }
    }

    public static class ActionConfiguration extends CommonConfiguration {

    }

    public static class CommonConfiguration {
        @NotEmpty
        private String type;

        private String name;

        private String description;

        public String getType() {
            return type;
        }

        public CommonConfiguration setType(String type) {
            this.type = type;
            return this;
        }

        public String getName() {
            return name;
        }

        public CommonConfiguration setName(String name) {
            this.name = name;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public CommonConfiguration setDescription(String description) {
            this.description = description;
            return this;
        }
    }
}

