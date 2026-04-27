package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private String keycloakId;
    private String login;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String email;
    private BigDecimal balance;
    private String password;
    private List<AccountsTransfer> accounts;
}