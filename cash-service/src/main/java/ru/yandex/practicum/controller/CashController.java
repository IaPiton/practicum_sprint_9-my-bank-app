package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.service.CashService;
import ru.yandex.practicum.cash.api.CashApi;
import ru.yandex.practicum.cash.model.CashDto;

@RestController
@RequiredArgsConstructor
public class CashController implements CashApi {
    private final CashService cashService;

    @Override
    public ResponseEntity<String> cash(CashDto cashDto) {
        try {
            String info = cashService.action(cashDto);
            return new ResponseEntity<>(info, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }
}