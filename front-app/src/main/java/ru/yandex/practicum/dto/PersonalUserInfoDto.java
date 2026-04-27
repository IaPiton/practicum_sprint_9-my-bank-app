package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.validator.BirthDay;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class PersonalUserInfoDto {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    @BirthDay
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate birthDate;
    @Email
    @NotNull
    private String email;
}