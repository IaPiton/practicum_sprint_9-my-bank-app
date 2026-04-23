package ru.yandex.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestTemplate;
import yandex.practicum.market.client.ApiClient;
import yandex.practicum.market.client.api.CashGetApi;
import yandex.practicum.market.client.api.CashPutApi;

import java.io.IOException;
import java.util.Objects;

@Configuration
public class AccountsClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService
                );

        manager.setAuthorizedClientProvider(authorizedClientProvider);
        return manager;
    }

    @Bean
    public RestTemplate restTemplate(OAuth2AuthorizedClientManager authorizedClientManager) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new OAuth2Interceptor(authorizedClientManager));
        return restTemplate;
    }

    @Bean
    public ApiClient apiClient(
            RestTemplate restTemplate,
            @Value("${bank.accounts-service.base-url}") String accountsServiceBaseUrl
    ) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(accountsServiceBaseUrl);
        return apiClient;
    }

    @Bean
    public CashPutApi cashPutApi(ApiClient apiClient) {
        return new CashPutApi(apiClient);
    }

    @Bean
    public CashGetApi cashGetApi(ApiClient apiClient) {
        return new CashGetApi(apiClient);
    }

    private record OAuth2Interceptor(
            OAuth2AuthorizedClientManager authorizedClientManager) implements ClientHttpRequestInterceptor {

        @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                        .withClientRegistrationId("cash-service")
                        .principal("cash-service")
                        .build();

                OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
                String accessToken = Objects.requireNonNull(authorizedClient).getAccessToken().getTokenValue();

                request.getHeaders().setBearerAuth(accessToken);
                return execution.execute(request, body);
            }
        }
}