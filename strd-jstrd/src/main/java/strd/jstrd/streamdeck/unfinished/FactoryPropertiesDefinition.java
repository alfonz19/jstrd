package strd.jstrd.streamdeck.unfinished;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class FactoryPropertiesDefinition {

    public static final FactoryPropertiesDefinition EMPTY = new FactoryPropertiesDefinition();

    private Set<PropertyDefinition> propertyDefinitions;

    public FactoryPropertiesDefinition addProperty(boolean required,
                                                   String propertyName,
                                                   PropertyDataType propertyDataType,
                                                   String description) {
        return addProperty(new PropertyDefinition(required,
                propertyName,
                propertyDataType,
                description));
    }

    public FactoryPropertiesDefinition addProperty(boolean required,
                                                   String propertyName,
                                                   PropertyDataType propertyDataType,
                                                   String description,
                                                   BiFunction<PropertyDefinition, Object, Optional<String>> customValidation) {
        return addProperty(new PropertyDefinition(required,
                propertyName,
                propertyDataType,
                description,
                customValidation));
    }

    public FactoryPropertiesDefinition addProperty(PropertyDefinition propertyDefinition) {
        propertyDefinitions.add(propertyDefinition);
        return this;
    }

    public Set<PropertyDefinition> getPropertyDefinitions() {
        return propertyDefinitions;
    }

    public static class PropertyDefinition {
        private final boolean required;
        private final String propertyName;
        private final PropertyDataType propertyDataType;
        private final String description;
        private final BiFunction<PropertyDefinition, Object, Optional<String>> customValidation;

        public PropertyDefinition(boolean required,
                                  String propertyName,
                                  PropertyDataType propertyDataType,
                                  String description) {
            this(required, propertyName, propertyDataType, description, (a, b) -> Optional.empty());
        }

        public PropertyDefinition(boolean required,
                                  String propertyName,
                                  PropertyDataType propertyDataType,
                                  String description,
                                  BiFunction<PropertyDefinition, Object, Optional<String>> customValidation) {
            this.required = required;
            this.propertyName = propertyName;
            this.description = description;
            this.propertyDataType = propertyDataType;
            this.customValidation = customValidation;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getDescription() {
            return description;
        }

        /**
         * @return true if {@value } is valid for this property.
         */
        public Optional<String> validateValue(Object value) {
            if (value == null && required) {
                return Optional.of(String.format("Missing required property %s", propertyName));
            }

            return customValidation.apply(this, value);
        }

        public PropertyDataType getPropertyDataType() {
            return propertyDataType;
        }

        public static BiFunction<PropertyDefinition, Object, Optional<String>> exceptionMessageAsErrorString(
                BiConsumer<PropertyDefinition, Object> propertyValueProcessor) {
            return (definition, value) -> {
                try {
                    propertyValueProcessor.accept(definition, value);
                    return Optional.empty();
                } catch (Exception e) {
                    return Optional.of(e.getMessage());
                }
            };
        }

        public static BiFunction<PropertyDefinition, Object, Optional<String>> exceptionMessageAsErrorString(
                Consumer<Object> propertyValueProcessor) {
            return (definition, value) -> {
                try {
                    propertyValueProcessor.accept(value);
                    return Optional.empty();
                } catch (Exception e) {
                    return Optional.of(e.getMessage());
                }
            };
        }
    }

    public enum PropertyDataType {
        STRING,
        LONG,
        DOUBLE,
        COLOR
    }
}
