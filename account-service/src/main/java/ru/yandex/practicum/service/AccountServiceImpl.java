package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.account.model.SignupUserInfoDto;
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
    public void creatUserAndAccount(String keycloakId, SignupUserInfoDto signupUserInfoDto) {
        BankUser bankUser = BankUser.builder()
                .username(signupUserInfoDto.getLogin())
                .birthday(signupUserInfoDto.getPersonalInfo().getBirthDate())
                .email(signupUserInfoDto.getPersonalInfo().getEmail())
                .firstName(signupUserInfoDto.getPersonalInfo().getFirstName())
                .lastName(signupUserInfoDto.getPersonalInfo().getLastName())
                .keycloakId(keycloakId)
                .fullName(signupUserInfoDto.getPersonalInfo().getFirstName() + " " + signupUserInfoDto.getPersonalInfo().getLastName())
                .build();
        bankUserRepository.save(bankUser);

        BankAccount bankAccount = BankAccount.builder()
                .bankUser(bankUser)
                .accountNumber(generateAccountNumber())
                .currency("RUB")
                .balance(0L)
                .isActive(true)
                .build();
        bankAccountRepository.save(bankAccount);

    }

    private String generateAccountNumber() {
        long milliseconds = System.currentTimeMillis();
        return "ACC" + milliseconds + UUID.randomUUID().toString().substring(0, 8);
    }


}
