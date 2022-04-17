package strd.jstrd.configuration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class LeafOrNonLeafValidationImpl implements ConstraintValidator<LeafOrNonLeafValidation, StreamDeckConfiguration.LeafOrNonLeaf> {

    @Override
    public void initialize(LeafOrNonLeafValidation constraint) {
        //not needed.
    }

    @Override
    public boolean isValid(StreamDeckConfiguration.LeafOrNonLeaf value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        int numberOfSetFields = (value.buttons == null ? 0 : 1) + (value.containers == null ? 0 : 1);
        return numberOfSetFields == 1;
    }
}
