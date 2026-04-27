package ru.yandex.practicum.handler;

public class CashException extends RuntimeException {
    public CashException(String message) {
        super(message);
    }
}
