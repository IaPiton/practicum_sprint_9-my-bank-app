package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import ru.yandex.practicum.account.api.UserApi;
import ru.yandex.practicum.account.model.UserDto;
import ru.yandex.practicum.service.AccountService;
import ru.yandex.practicum.service.CreateUserService;
import ru.yandex.practicum.service.UserService;


@Controller
@RequiredArgsConstructor
public class UserController
        implements UserApi
{
    private final CreateUserService createUserService;
    private final AccountService accountService;
    private final UserService userService;

    @Override
    public ResponseEntity<UserDto> getUserByKeycloakId(String keycloakId) {
        return ResponseEntity.ok(userService.getUser(keycloakId));
    }

    @Override
    public ResponseEntity<Void> registerNewUser(UserDto userDto) {
        try {
            createUserService.addUser(userDto);
            accountService.createUserAndAccount(userDto);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
