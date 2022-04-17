package strd.jstrd.streamdeck.unfinished.button;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

public interface ButtonFactory {
    String getButtonName();
    Button create(Map<String, Object> properties);
    ButtonConfigurationDefinition getButtonConfigurationDefinition();

    class ButtonConfigurationDefinition {

        public static final ButtonConfigurationDefinition EMPTY = new ButtonConfigurationDefinition();

        private Set<ButtonPropertyDefinition> propertyDefinitions;

        public ButtonConfigurationDefinition addProperty(String propertyName,
                                                         ButtonPropertyDataType buttonPropertyDataType,
                                                         String description) {
            return addProperty(new ButtonPropertyDefinition(propertyName, buttonPropertyDataType, description));
        }

        public ButtonConfigurationDefinition addProperty(ButtonPropertyDefinition propertyDefinition) {
            propertyDefinitions.add(propertyDefinition);
            return this;
        }

        public static class ButtonPropertyDefinition {
            private String propertyName;
            private ButtonPropertyDataType buttonPropertyDataType;
            private String description;

            public ButtonPropertyDefinition() {
            }

            public ButtonPropertyDefinition(String propertyName,
                                            ButtonPropertyDataType buttonPropertyDataType,
                                            String description) {
                this.propertyName = propertyName;
                this.description = description;
                this.buttonPropertyDataType = buttonPropertyDataType;
            }

            public String getPropertyName() {
                return propertyName;
            }

            public String getDescription() {
                return description;
            }

            public ButtonPropertyDataType getButtonPropertyDataType() {
                return buttonPropertyDataType;
            }
        }

        public enum ButtonPropertyDataType {
            STRING,
            LONG,
            DOUBLE,
            COLOR
        }
    }

}
