package ru.yandex.practicum.dto;

import lombok.Getter;
import org.apache.commons.lang3.BooleanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ErrorStorage {

    private final List<String> errors = new ArrayList<>();

    public void addError(String error) {
        errors.add(error);
    }

    public boolean hasError() {
        return BooleanUtils.isFalse(errors.isEmpty());
    }
}