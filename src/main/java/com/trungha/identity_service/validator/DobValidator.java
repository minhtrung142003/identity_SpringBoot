package com.trungha.identity_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate>{

    private int min;
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if(Objects.isNull(value))
            return true;

        // check so nam now co bang so nam ghi vao
       long years =  ChronoUnit.YEARS.between(value, LocalDate.now());
        return years >= min;
    }

    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min(); // khai bao min ở trên va so sánh min của DobConstraint
    }
}
