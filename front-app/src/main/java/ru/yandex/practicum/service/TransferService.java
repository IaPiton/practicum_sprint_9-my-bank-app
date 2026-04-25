package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.dto.TransferDto;
import ru.yandex.practicum.handler.AccountException;
import ru.yandex.practicum.handler.UnauthorizedException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final WebClient gatewayWebClient;
    @Value("${bank.gateway.base-url}")
    private String gatewayBaseUrl;

    public void transferCash(Model model, String keycloakId, int value, String login) {
        ResponseEntity<String> response;
        try {
            response = gatewayWebClient
                    .post()
                    .uri(gatewayBaseUrl + "/transfer/send")
                    .bodyValue(TransferDto.builder()
                            .keycloakId(keycloakId)
                            .login(login)
                            .value(value)
                            .build())
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            model.addAttribute("info", Objects.requireNonNull(response).getBody());

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().toString().startsWith("401")) {
                throw new UnauthorizedException();
            } else if (e.getStatusCode().toString().startsWith("400")) {
                model.addAttribute("errors", e.getResponseBodyAsString());
            }
        } catch (Exception e) {
            throw new AccountException("Ошибка при переводе. Повторите запрос позже.");
        }
    }
}
