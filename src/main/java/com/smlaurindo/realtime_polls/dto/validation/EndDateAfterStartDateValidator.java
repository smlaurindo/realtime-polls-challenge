package com.smlaurindo.realtime_polls.dto.validation;

import com.smlaurindo.realtime_polls.dto.request.CreatePollRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndDateAfterStartDateValidator implements ConstraintValidator<EndDateAfterStartDate, CreatePollRequest> {

    @Override
    public boolean isValid(CreatePollRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        if (request.startsAt() == null || request.endsAt() == null) return true;

        boolean isValid = request.endsAt().isAfter(request.startsAt());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("endsAt")
                    .addConstraintViolation();
        }

        return isValid;
    }
}