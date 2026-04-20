package ru.yandex.practicum.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountCurrency {
    RUB("Рубли"),
    CNY("Юани"),
    USD("Доллары");

    private final String title;
}