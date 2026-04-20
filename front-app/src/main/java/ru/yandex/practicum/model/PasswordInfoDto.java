package ru.yandex.practicum.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public final class PasswordInfoDto {
    @NotNull
    private String password;
    @NotNull
    private String confirmPassword;
}
