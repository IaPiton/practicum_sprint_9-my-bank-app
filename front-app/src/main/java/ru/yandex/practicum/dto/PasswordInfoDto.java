package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.yandex.practicum.validator.PasswordConfirmed;

@Data
@PasswordConfirmed
public final class PasswordInfoDto {
    @NotNull
    private String password;
    @NotNull
    private String confirmPassword;
}
