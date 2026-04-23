package ru.yandex.practicum.service;

import ru.yandex.practicum.account.model.UserDto;

public interface UserService {
    
    UserDto getUser(String keycloakId);

    String updateUser(UserDto userDto);
}
