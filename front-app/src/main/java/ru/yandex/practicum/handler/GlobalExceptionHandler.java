package ru.yandex.practicum.handler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleValidationExceptions(ConstraintViolationException ex, Model model) {
        List<String> errors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> {
            String message = violation.getMessage();
            errors.add(message);
        });
        model.addAttribute("errors", errors);
        return "main";
    }

    @ExceptionHandler(AccountException.class)
    public String handleAccountExceptions(AccountException ex, Model model) {
        model.addAttribute("errors", ex.getMessage());
        return "main";
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedExceptions(UnauthorizedException ex, Model model) {
        model.addAttribute("errors", ex.getMessage());
        return "signup";
    }
}