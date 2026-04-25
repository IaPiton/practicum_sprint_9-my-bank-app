package ru.yandex.practicum.configuration;

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
import yandex.practicum.market.client.api.NotificationApi;

import java.io.IOException;
import java.util.Objects;

@Configuration
public class NotificationClientConfig {

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
            @Value("${bank.notification-service.base-url}") String notificationServiceBaseUrl
    ) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(notificationServiceBaseUrl);
        return apiClient;
    }

    @Bean
    public NotificationApi notificationApi(ApiClient apiClient) {
        return new NotificationApi(apiClient);
    }

    private record OAuth2Interceptor(
            OAuth2AuthorizedClientManager authorizedClientManager) implements ClientHttpRequestInterceptor {

        @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                        .withClientRegistrationId("account-service")
                        .principal("account-service")
                        .build();

                OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
                String accessToken = Objects.requireNonNull(authorizedClient).getAccessToken().getTokenValue();

                request.getHeaders().setBearerAuth(accessToken);
                return execution.execute(request, body);
            }
        }
}