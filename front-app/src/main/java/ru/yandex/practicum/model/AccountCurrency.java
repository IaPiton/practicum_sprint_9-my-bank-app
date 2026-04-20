package ru.yandex.practicum.model;

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