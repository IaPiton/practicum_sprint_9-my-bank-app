package ru.yandex.practicum.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.notification.api.NotificationApi;
import ru.yandex.practicum.notification.model.Notification;
import ru.yandex.practicum.service.NotificationService;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {
    private final NotificationService notificationService;

    @Override
    public ResponseEntity<Void> notification(Notification notification) {
        notificationService.send(notification);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
