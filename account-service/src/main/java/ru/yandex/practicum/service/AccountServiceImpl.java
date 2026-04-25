package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.handler.CashException;
import ru.yandex.practicum.model.CashAccountDto;
import ru.yandex.practicum.model.UserDto;
import ru.yandex.practicum.persistent.entity.BankAccount;
import ru.yandex.practicum.persistent.entity.BankUser;
import ru.yandex.practicum.persistent.mapper.BankUserMapper;
import ru.yandex.practicum.persistent.repository.BankAccountRepository;
import ru.yandex.practicum.persistent.repository.BankUserRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final BankAccountRepository bankAccountRepository;
    private final BankUserRepository bankUserRepository;
    private final BankUserMapper bankUserMapper;

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

    @Override
    public UserDto putCash(CashAccountDto cashAccountDto) {
        BankUser bankUser = bankUserRepository.findByKeycloakId(cashAccountDto.getKeycloakId()).orElseThrow(RuntimeException::new);
        BigDecimal balance = bankUser.getBankAccount().getBalance().add(BigDecimal.valueOf(Long.parseLong(cashAccountDto.getValue())));
        bankUser.getBankAccount().setBalance(balance);
        bankUserRepository.save(bankUser);
        return bankUserMapper.toDto(bankUser);
    }

    @Override
    public UserDto getCash(CashAccountDto cashAccountDto) {
        BankUser bankUser = bankUserRepository.findByKeycloakId(cashAccountDto.getKeycloakId()).orElseThrow(RuntimeException::new);

        BigDecimal currentBalance = bankUser.getBankAccount().getBalance();
        BigDecimal amount = BigDecimal.valueOf(Long.parseLong(cashAccountDto.getValue()));

        if (currentBalance.compareTo(amount) < 0) {
            throw new CashException(
                    String.format("Недостаточно средств. Доступно: %.2f, Запрошено: %.2f",
                            currentBalance, amount)
            );
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        bankUser.getBankAccount().setBalance(newBalance);
        bankUserRepository.save(bankUser);
        return bankUserMapper.toDto(bankUser);
    }

    private String generateAccountNumber() {
        long milliseconds = System.currentTimeMillis();
        return "ACC" + milliseconds + UUID.randomUUID().toString().substring(0, 8);
    }


}
