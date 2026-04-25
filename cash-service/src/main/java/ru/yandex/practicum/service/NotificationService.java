package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yandex.practicum.notification.client.api.NotificationApi;
import yandex.practicum.notification.client.model.Notification;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationApi notificationApi;

    public void sendNotification(String email, String heading, String text) {
        Notification notification = new Notification();
        notification.setEmail(email);
        notification.setHeading(heading);
        notification.setText(text);
        try {
            notificationApi.notification(notification);
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления {}", e.getMessage());
        }
    }
}
