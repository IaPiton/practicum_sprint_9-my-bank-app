package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import ru.yandex.practicum.exception.TransferException;

import ru.yandex.practicum.model.AccountsTransfer;
import ru.yandex.practicum.model.TransferDto;
import ru.yandex.practicum.model.UserDto;
import yandex.practicum.account.client.api.TransferAccountApi;


import java.util.Objects;


@Service
@RequiredArgsConstructor
public class TransferService {

    private final NotificationService notificationService;

    private final TransferAccountApi transferApi;

    public String transfer(TransferDto transferDto) {
        String nameUserTransfer;
        try {
            UserDto userDto = transferApi.transfer(transferDto);
            nameUserTransfer = Objects.requireNonNull(userDto.getAccounts())
                    .stream()
                    .filter(accountsTransfer -> accountsTransfer.getLogin().equals(transferDto.getLogin()))
                    .findFirst()
                    .map(AccountsTransfer::getName)
                    .orElse(null);

            notificationService.sendNotification(
                    Objects.requireNonNull(userDto).getEmail(),
                    "Перевод средств",
                    String.format("Уважаемый %s %s, перевод %s руб. на счет %s выполнен!",
                            userDto.getLastName(),
                            userDto.getFirstName(),
                            transferDto.getValue(),
                            nameUserTransfer
                    ));
        } catch (
                HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new TransferException(e.getResponseBodyAsString());
            } else {
                throw new TransferException("Ошибка при переводе");
            }
        } catch (Exception e) {
            throw new TransferException("Ошибка при переводе");
        }

        return String.format("Успешно переведено %s руб. клиенту %s", transferDto.getValue(), nameUserTransfer);
    }

}
