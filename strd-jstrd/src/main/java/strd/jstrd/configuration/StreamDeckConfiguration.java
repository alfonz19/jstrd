package strd.jstrd.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamDeckConfiguration {

    @Valid
    public List<DeviceConfiguration> devices;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeviceConfiguration {
        public String serialNumber;

        public Duration updateInterval;

        @Valid
        public LeafOrNonLeaf layout;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @LeafOrNonLeafValidation
    public static class LeafOrNonLeaf {
        @Valid
        public List<NonLeaf> groups;
        public List<ButtonConfiguration> buttons;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NonLeaf extends LeafOrNonLeaf {
        public String name;
        public String condition;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ButtonConfiguration {
        public String index;
        public String name;
        public String description;
        public String buttonType;
        public Map<String, String> configuration;
    }
}

