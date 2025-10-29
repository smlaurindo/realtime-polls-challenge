package com.smlaurindo.realtime_polls.validation.validator;

import com.smlaurindo.realtime_polls.dto.CreatePollRequest;
import com.smlaurindo.realtime_polls.validation.annotation.EndDateAfterStartDate;
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