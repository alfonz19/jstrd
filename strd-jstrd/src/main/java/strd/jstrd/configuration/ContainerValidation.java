package strd.jstrd.configuration;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ContainerValidationImpl.class)
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public @interface ContainerValidation {

    String message() default "only either buttons or groups can be specified.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}