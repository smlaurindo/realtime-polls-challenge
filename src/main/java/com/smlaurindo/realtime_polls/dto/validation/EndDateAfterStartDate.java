package com.smlaurindo.realtime_polls.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EndDateAfterStartDateValidator.class)
@Documented
public @interface EndDateAfterStartDate {
    String message() default "The end date must be after the start date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}