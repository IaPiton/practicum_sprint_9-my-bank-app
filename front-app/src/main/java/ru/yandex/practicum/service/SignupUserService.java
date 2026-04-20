package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.dto.ErrorStorage;
import ru.yandex.practicum.dto.SignupUserInfoDto;

@Service
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
                            .bodyValue(userInfo)
                            .retrieve()
                            .bodyToMono(ErrorStorage.class)
                            .block();
        } catch (Exception e) {
            errorStorage.addError("Не удалось зарегистрировать пользователя, попробуйте позже");
        }

        return errorStorage;
    }
}
