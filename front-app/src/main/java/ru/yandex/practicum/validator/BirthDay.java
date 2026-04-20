package ru.yandex.practicum.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BirthDayUserValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface BirthDay {
    String message() default "Доступ к сервису доступен лишь по достижению 18-ти лет";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}