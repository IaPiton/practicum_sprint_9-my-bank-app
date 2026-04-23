package ru.yandex.practicum;

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

@Service
@RequiredArgsConstructor
public class CashService {
    private final CashGetApi cashGetApi;
    private final CashPutApi cashPutApi;

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

        ResponseEntity<UserDto> userDto = cashPutApi.cashPutWithHttpInfo(cashAccountDto);
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

            ResponseEntity<UserDto> userDto = cashGetApi.cashGetWithHttpInfo(cashAccountDto);
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
