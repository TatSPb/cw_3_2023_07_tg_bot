package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }
    @Transactional
    public void save(long chat_id, String text, LocalDateTime localDateTime){
        notificationTaskRepository.save(new NotificationTask(
                text,
                chat_id,
                localDateTime.truncatedTo(ChronoUnit.MINUTES))
        );
    }
}
