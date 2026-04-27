package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.notification.model.Notification;

@Slf4j
@Service
public class NotificationService {

    public void send(Notification notification){
        log.info("""
                Получатель: {}
                Заголовок: {}
                Текст: {}
                """, notification.getEmail(), notification.getHeading(), notification.getText());
    }
}
