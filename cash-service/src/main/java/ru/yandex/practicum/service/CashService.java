package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ru.yandex.practicum.cash.model.CashDto;
import ru.yandex.practicum.exception.CashException;
import yandex.practicum.market.client.api.CashGetApi;
import yandex.practicum.market.client.api.CashPutApi;
import yandex.practicum.market.client.model.CashAccountDto;
import yandex.practicum.market.client.model.UserDto;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CashService {
    private final CashGetApi cashGetApi;
    private final CashPutApi cashPutApi;
    private final NotificationService notificationService;

    public String action(CashDto cashDto) {
        CashAccountDto cashAccountDto = new CashAccountDto();
        cashAccountDto.setKeycloakId(cashDto.getKeycloakId());
        cashAccountDto.setValue(cashDto.getValue());
        return switch (cashDto.getAction()) {
            case "GET" -> actionGet(cashAccountDto);
            case "PUT" -> actionPut(cashAccountDto);
            default -> throw new CashException("Ошибка при операции со счетом");
        };
    }

    private String actionPut(CashAccountDto cashAccountDto) {
        try {

        ResponseEntity<UserDto> response = cashPutApi.cashPutWithHttpInfo(cashAccountDto);
        UserDto userDto = response.getBody();
        notificationService.sendNotification(
                    Objects.requireNonNull(userDto).getEmail(),
                    "Пополнение счета",
                    String.format("Уважаемый %s %s, ваш счет пополнен на %s руб.!", userDto.getLastName(), userDto.getFirstName(), cashAccountDto.getValue()));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new CashException(e.getResponseBodyAsString());
            } else {
                throw new CashException("Ошибка при операции со счетом");
            }
        } catch (Exception e) {
            throw new CashException("Ошибка при операции со счетом");
        }

        return "Положено " + cashAccountDto.getValue() + " руб.";
    }

    private String actionGet(CashAccountDto cashAccountDto) {
        try {
            ResponseEntity<UserDto> response = cashGetApi.cashGetWithHttpInfo(cashAccountDto);
            UserDto userDto = response.getBody();
            notificationService.sendNotification(
                    Objects.requireNonNull(userDto).getEmail(),
                    "Пополнение счета",
                    String.format("Уважаемый %s %s, с вашего счета снято %s руб.!", userDto.getLastName(), userDto.getFirstName(), cashAccountDto.getValue()));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new CashException(e.getResponseBodyAsString());
            } else {
                throw new CashException("Ошибка при операции со счетом");
            }
        } catch (Exception e) {
            throw new CashException("Ошибка при операции со счетом");
        }
        return "Снято " + cashAccountDto.getValue() + " руб.";
    }


}
