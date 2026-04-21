package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.account.model.UserDto;
import ru.yandex.practicum.persistent.entity.BankAccount;
import ru.yandex.practicum.persistent.entity.BankUser;
import ru.yandex.practicum.persistent.repository.BankAccountRepository;
import ru.yandex.practicum.persistent.repository.BankUserRepository;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final BankAccountRepository bankAccountRepository;
    private final BankUserRepository bankUserRepository;

    @Override
    public void createUserAndAccount(UserDto userDto) {
        BankUser bankUser = BankUser.builder()
                .username(userDto.getLogin())
                .birthday(userDto.getBirthday())
                .email(userDto.getEmail())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .keycloakId(userDto.getKeycloakId())
                .build();
        bankUserRepository.save(bankUser);

        BankAccount bankAccount = BankAccount.builder()
                .bankUser(bankUser)
                .accountNumber(generateAccountNumber())
                .currency("RUB")
                .balance(userDto.getBalance())
                .isActive(true)
                .build();
        bankAccountRepository.save(bankAccount);

    }

    private String generateAccountNumber() {
        long milliseconds = System.currentTimeMillis();
        return "ACC" + milliseconds + UUID.randomUUID().toString().substring(0, 8);
    }


}
