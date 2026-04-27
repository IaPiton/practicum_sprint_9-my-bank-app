package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.model.CashAccountDto;
import ru.yandex.practicum.model.TransferDto;
import ru.yandex.practicum.model.UserDto;
import ru.yandex.practicum.persistent.entity.BankUser;
import ru.yandex.practicum.persistent.repository.BankUserRepository;


@Service
@RequiredArgsConstructor
public class TransferService {
    private final AccountService accountService;
    private final BankUserRepository bankUserRepository;

    public UserDto transfer(TransferDto transferDto) {
        CashAccountDto user = new CashAccountDto();
        user.setKeycloakId(transferDto.getKeycloakId());
        user.setValue(transferDto.getValue());
        UserDto userDto = accountService.getCash(user);

        CashAccountDto transfer = new CashAccountDto();
        BankUser bankUserTransfer = bankUserRepository.findByUsername(transferDto.getLogin()).orElseThrow(RuntimeException::new);
        transfer.setKeycloakId(bankUserTransfer.getKeycloakId());
        transfer.setValue(transferDto.getValue());
        accountService.putCash(transfer);

        return userDto;
    }
}
