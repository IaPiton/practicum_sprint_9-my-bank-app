package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


import ru.yandex.practicum.account.api.CashGetApi;
import ru.yandex.practicum.account.api.CashPutApi;
import ru.yandex.practicum.handler.CashException;

import ru.yandex.practicum.model.CashAccountDto;
import ru.yandex.practicum.model.UserDto;
import ru.yandex.practicum.service.AccountService;

@RestController
@RequiredArgsConstructor
public class CashController implements CashGetApi, CashPutApi {
    private final AccountService accountService;
    @Override
    public ResponseEntity<UserDto> cashGet(CashAccountDto cashAccountDto) {
        try {
            return new ResponseEntity<>(accountService.getCash(cashAccountDto), HttpStatus.OK);
        } catch (CashException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ошибка при снятии денег со счета");
        }
    }

    @Override
    public ResponseEntity<UserDto> cashPut(CashAccountDto cashAccountDto) {
        try {
         return new ResponseEntity<>(accountService.putCash(cashAccountDto), HttpStatus.OK);
        } catch (CashException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ошибка при пополнении счета");
        }
    }
}
