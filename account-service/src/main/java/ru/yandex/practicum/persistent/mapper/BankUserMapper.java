package ru.yandex.practicum.persistent.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.account.model.UserDto;
import ru.yandex.practicum.persistent.entity.BankUser;


@Mapper(componentModel = "spring")
public interface BankUserMapper {
    @Mapping(source = "username", target = "login")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "balance", expression = "java(bankUser.getBankAccount() != null ? bankUser.getBankAccount().getBalance() : null)")
    UserDto toDto(BankUser bankUser);
}

