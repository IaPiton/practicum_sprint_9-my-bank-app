package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


import ru.yandex.practicum.account.api.TransferAccountApi;
import ru.yandex.practicum.handler.CashException;
import ru.yandex.practicum.model.TransferDto;
import ru.yandex.practicum.model.UserDto;
import ru.yandex.practicum.service.TransferService;
import ru.yandex.practicum.service.UserService;


@RestController
@RequiredArgsConstructor
public class TransferController implements TransferAccountApi {
    private final UserService userService;
    private final TransferService transferService;

    @Override
    public ResponseEntity<UserDto> transfer(TransferDto transferDto) {
        try {
            UserDto userDto = transferService.transfer(transferDto);
            return ResponseEntity.ok(userService.getUser(userDto.getKeycloakId()));
        } catch (CashException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ошибка при переводе денег со счета");
        }
    }

}