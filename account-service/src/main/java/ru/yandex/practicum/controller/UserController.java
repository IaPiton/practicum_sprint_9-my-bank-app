package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import ru.yandex.practicum.account.api.UserApi;
import ru.yandex.practicum.account.model.SignupUserInfoDto;
import ru.yandex.practicum.service.AccountService;
import ru.yandex.practicum.service.CreateUserService;


@Controller
@RequiredArgsConstructor
public class UserController
        implements UserApi
{
    private final CreateUserService createUserService;
    private final AccountService accountService;

    @Override
    public ResponseEntity<Void> registerNewUser(SignupUserInfoDto signupUserInfoDto) {
        try {
            String keycloakId = createUserService.addUser(signupUserInfoDto);
            accountService.creatUserAndAccount(keycloakId, signupUserInfoDto);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
