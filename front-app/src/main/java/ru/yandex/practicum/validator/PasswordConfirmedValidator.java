package ru.yandex.practicum.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.dto.PasswordInfoDto;

import java.util.Objects;

public class PasswordConfirmedValidator implements ConstraintValidator<PasswordConfirmed, PasswordInfoDto> {

    @Override
    public void initialize(PasswordConfirmed constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(PasswordInfoDto passwordInfo, ConstraintValidatorContext constraintValidatorContext) {
        return Objects.equals(passwordInfo.getPassword(), passwordInfo.getConfirmPassword());
    }
}