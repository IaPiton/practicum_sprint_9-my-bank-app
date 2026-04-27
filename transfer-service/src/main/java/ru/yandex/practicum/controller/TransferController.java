package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import ru.yandex.practicum.model.TransferDto;
import ru.yandex.practicum.service.TransferService;
import ru.yandex.practicum.transfer.api.TransferApi;


@RestController
@RequiredArgsConstructor
public class TransferController implements TransferApi {
    private final TransferService transferService;

    @Override
    public ResponseEntity<String> transfer(TransferDto transferDto) {
                try {
            String info = transferService.transfer(transferDto);
            return new ResponseEntity<>(info, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}