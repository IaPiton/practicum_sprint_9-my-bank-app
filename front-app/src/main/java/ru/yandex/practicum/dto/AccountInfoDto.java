package ru.yandex.practicum.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class AccountInfoDto {
    private AccountCurrency currency;
    private BigDecimal value;
    private boolean exists;

    public static AccountInfoDto newAccount(AccountCurrency currency) {
        return new AccountInfoDto().setCurrency(currency).setValue(BigDecimal.ZERO).setExists(false);
    }
}