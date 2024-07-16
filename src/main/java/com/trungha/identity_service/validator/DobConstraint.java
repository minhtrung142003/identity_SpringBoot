package com.trungha.identity_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = { DobValidator.class})
public @interface DobConstraint {

    int min();

    String message() default "{Invalid date of birth}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}