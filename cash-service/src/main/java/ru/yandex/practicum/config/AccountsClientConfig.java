package ru.yandex.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import yandex.practicum.account.client.ApiClient;
import yandex.practicum.account.client.api.CashGetApi;
import yandex.practicum.account.client.api.CashPutApi;


@Configuration
public class AccountsClientConfig {

    @Bean
    public ApiClient apiAccountClient(
            RestTemplate restTemplate,
            @Value("${bank.accounts-service.base-url}") String accountsServiceBaseUrl
    ) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(accountsServiceBaseUrl);
        return apiClient;
    }

    @Bean
    public CashPutApi cashPutApi(ApiClient apiAccountClient) {
        return new CashPutApi(apiAccountClient);
    }

    @Bean
    public CashGetApi cashGetApi(ApiClient apiAccountClient) {
        return new CashGetApi(apiAccountClient);
    }
}