package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.account.model.UserDto;
import ru.yandex.practicum.persistent.entity.BankUser;
import ru.yandex.practicum.persistent.mapper.BankUserMapper;
import ru.yandex.practicum.persistent.repository.BankUserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final BankUserRepository bankUserRepository;
    private final BankUserMapper bankUserMapper;

    public UserDto getUser(String keycloakId) {
        BankUser bankUser = bankUserRepository.findByKeycloakId(keycloakId).orElseThrow(RuntimeException::new);
        return bankUserMapper.toDto(bankUser);
    }
}
