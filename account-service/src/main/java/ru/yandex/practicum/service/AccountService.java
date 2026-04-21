package ru.yandex.practicum.service;

import ru.yandex.practicum.account.model.UserDto;

public interface AccountService {
    void createUserAndAccount(UserDto userDto);
}
