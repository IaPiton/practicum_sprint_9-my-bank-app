package ru.yandex.practicum.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class BirthDayUserValidator implements ConstraintValidator<BirthDay, LocalDate> {

    @Override
    public void initialize(BirthDay constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        return Objects.isNull(localDate) || ChronoUnit.YEARS.between(localDate, LocalDate.now()) >= 18;
    }
}