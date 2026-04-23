package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.account.model.UserDto;
import ru.yandex.practicum.persistent.entity.BankUser;
import ru.yandex.practicum.persistent.mapper.BankUserMapper;
import ru.yandex.practicum.persistent.repository.BankUserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final BankUserRepository bankUserRepository;
    private final BankUserMapper bankUserMapper;

    public UserDto getUser(String keycloakId) {
        BankUser bankUser = bankUserRepository.findByKeycloakId(keycloakId).orElseThrow(RuntimeException::new);
        return bankUserMapper.toDto(bankUser);
    }

    @Override
    public String updateUser(UserDto userDto) {
        BankUser bankUser = bankUserRepository.findByKeycloakId(userDto.getKeycloakId()).orElseThrow(RuntimeException::new);
        bankUser.setBirthday(userDto.getBirthday());
        bankUser.setFirstName(userDto.getFirstName());
        bankUser.setLastName(userDto.getLastName());
        bankUserRepository.save(bankUser);
        return "Пользователь успешно обновлен.";
    }
}
