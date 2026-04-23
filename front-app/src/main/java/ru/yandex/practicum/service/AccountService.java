package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.dto.Account;
import ru.yandex.practicum.handler.AccountException;
import ru.yandex.practicum.handler.UnauthorizedException;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountService {
    @Value("${bank.gateway.base-url}")
    private String gatewayBaseUrl;

    private final WebClient gatewayWebClient;

    public void getAccount(String keycloakId, Model model) {
        try {
            ResponseEntity<Account> response = gatewayWebClient
                    .get()
                    .uri(gatewayBaseUrl + "/account/user/{keycloakId}", keycloakId)
                    .retrieve()
                    .toEntity(Account.class)
                    .block();
            if (Objects.requireNonNull(response).getStatusCode().value() == 401) {
                throw new UnauthorizedException();
            }

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Account account = response.getBody();
                model.addAttribute("fullName", account.getLastName() + " " + account.getFirstName());
                model.addAttribute("birthdate", account.getBirthday());
                model.addAttribute("sum", account.getBalance());
            }

        } catch (Exception e) {
            throw new AccountException("Ошибка при получении информации об аккаунте");
        }
    }

    public String updateAccount(String keycloakId, String fullName, LocalDate birthdate) {
        String[] name = fullName.split(" ");
        try {
            ResponseEntity<String> response = gatewayWebClient
                    .post()
                    .uri(gatewayBaseUrl + "/account/user")
                    .bodyValue(Account.builder()
                            .keycloakId(keycloakId)
                            .birthday(birthdate)
                            .firstName(name.length >= 2 ? name[1].trim() : null)
                            .lastName(name.length >= 1 ? name[0].trim() : null)
                            .build())
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            if (Objects.requireNonNull(response).getStatusCode().value() == 401) {
                throw new UnauthorizedException();
            }

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

        } catch (Exception e) {
            throw new AccountException("Ошибка при обновлении аккаунта");
        }
        throw new AccountException("Ошибка при обновлении аккаунта");
    }

}