package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.model.AccountsTransfer;
import ru.yandex.practicum.model.UserDto;
import ru.yandex.practicum.persistent.entity.BankUser;
import ru.yandex.practicum.persistent.mapper.BankUserMapper;
import ru.yandex.practicum.persistent.repository.BankUserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final BankUserRepository bankUserRepository;
    private final BankUserMapper bankUserMapper;
    private final NotificationService notificationService;

    public UserDto getUser(String keycloakId) {
        BankUser bankUser = bankUserRepository.findByKeycloakId(keycloakId).orElseThrow(RuntimeException::new);
        UserDto userDto = bankUserMapper.toDto(bankUser);

        List<BankUser> bankUsers = bankUserRepository.findAll();
        bankUsers.remove(bankUser);

        List<AccountsTransfer> accountsTransfer = bankUsers.stream()
                .map(user -> new AccountsTransfer(user.getUsername(), user.getLastName() + " " + user.getFirstName()))
                .toList();
        userDto.setAccounts(accountsTransfer);

        return userDto;
    }

    @Override
    public String updateUser(UserDto userDto) {
        BankUser bankUser = bankUserRepository.findByKeycloakId(userDto.getKeycloakId()).orElseThrow(RuntimeException::new);
        bankUser.setBirthday(userDto.getBirthday());
        bankUser.setFirstName(userDto.getFirstName());
        bankUser.setLastName(userDto.getLastName());
        bankUserRepository.save(bankUser);
        notificationService.sendNotification(
                bankUser.getEmail(),
                "Обновление учетной записи",
                String.format("Уважаемый %s %s, ваши данные учетной записи обновлены!", bankUser.getLastName(), bankUser.getFirstName()));
        return "Пользователь успешно обновлен.";
    }
}
