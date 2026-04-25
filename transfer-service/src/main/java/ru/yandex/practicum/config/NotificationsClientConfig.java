package ru.yandex.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.web.client.RestTemplate;
import yandex.practicum.notification.client.ApiClient;
import yandex.practicum.notification.client.api.NotificationApi;


@Configuration
public class NotificationsClientConfig {

    @Bean
    public ApiClient apiNotificationClient(
            RestTemplate restTemplate,
            @Value("${bank.notification-service.base-url}") String notificationServiceBaseUrl
    ) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(notificationServiceBaseUrl);
        return apiClient;
    }

    @Bean
    public NotificationApi sendNotification(ApiClient apiNotificationClient) {
        return new NotificationApi(apiNotificationClient);
    }

}