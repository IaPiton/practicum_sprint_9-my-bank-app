package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.dto.Account;
import ru.yandex.practicum.dto.ErrorStorage;
import ru.yandex.practicum.dto.SignupUserInfoDto;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignupUserService {
    private final WebClient gatewayWebClient;
    @Value("${bank.gateway.base-url}")
    private String gatewayBaseUrl;

    public ErrorStorage signupNewUser(SignupUserInfoDto userInfo) {
        ErrorStorage errorStorage = new ErrorStorage();
        try {
                    gatewayWebClient
                            .post()
                            .uri(gatewayBaseUrl + "/account/user/register")
                            .bodyValue(Account.builder()
                                    .birthday(userInfo.getPersonalInfo().getBirthDate())
                                    .firstName(userInfo.getPersonalInfo().getFirstName())
                                    .lastName(userInfo.getPersonalInfo().getLastName())
                                    .email(userInfo.getPersonalInfo().getEmail())
                                    .login(userInfo.getLogin())
                                    .password(userInfo.getPasswordInfo().getPassword())
                                    .balance(new BigDecimal(0))
                                    .build())
                            .retrieve()
                            .bodyToMono(Account.class)
                            .block();
        } catch (Exception e) {
            log.error(e.getMessage());
            errorStorage.addError("Не удалось зарегистрировать пользователя, попробуйте позже");
        }
        return errorStorage;
    }
}
