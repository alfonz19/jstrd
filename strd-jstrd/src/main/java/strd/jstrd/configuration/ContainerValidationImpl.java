package strd.jstrd.configuration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class ContainerValidationImpl implements ConstraintValidator<ContainerValidation, StreamDeckConfiguration.ContainerConfiguration> {

    @Override
    public void initialize(ContainerValidation constraint) {
        //not needed.
    }

    @Override
    public boolean isValid(StreamDeckConfiguration.ContainerConfiguration value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        int numberOfSetFields = (value.buttons == null ? 0 : 1) + (value.containers == null ? 0 : 1);
        return numberOfSetFields == 1;
    }
}
