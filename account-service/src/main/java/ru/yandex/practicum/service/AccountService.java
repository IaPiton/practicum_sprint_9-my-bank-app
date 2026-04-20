package ru.yandex.practicum.service;

import ru.yandex.practicum.account.model.SignupUserInfoDto;

public interface AccountService {
    void creatUserAndAccount(String keycloakId, SignupUserInfoDto signupUserInfoDto);
}
