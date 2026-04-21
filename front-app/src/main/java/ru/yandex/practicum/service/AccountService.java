package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.dto.Account;

@Service
@RequiredArgsConstructor
public class AccountService {
    @Value("${bank.gateway.base-url}")
    private String gatewayBaseUrl;

    private final WebClient gatewayWebClient;

    public Account getAccount(String keycloakId) {
        try {
            return gatewayWebClient
                    .get()
                    .uri(gatewayBaseUrl + "/account/user/{keycloakId}", keycloakId)
                    .retrieve()
                    .bodyToMono(Account.class)
                    .block();

        } catch (Exception e) {
            return new Account();
        }
    }

}
