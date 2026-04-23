package ru.yandex.practicum.service;

import ru.yandex.practicum.account.model.CashAccountDto;
import ru.yandex.practicum.account.model.UserDto;

public interface AccountService {
    void createUserAndAccount(UserDto userDto);
    UserDto putCash(CashAccountDto cashAccountDto);
    UserDto getCash(CashAccountDto cashAccountDto);
}
