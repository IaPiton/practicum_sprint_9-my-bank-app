package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Account {
    private String keycloakId;
    private String login;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String email;
    private BigDecimal balance;
    private String password;
}