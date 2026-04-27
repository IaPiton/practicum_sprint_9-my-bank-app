package ru.yandex.practicum.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignupUserInfoDto {
    @NotNull
    private String login;
    @Valid
    private PasswordInfoDto passwordInfo = new PasswordInfoDto();
    @Valid
    private PersonalUserInfoDto personalInfo = new PersonalUserInfoDto();
}