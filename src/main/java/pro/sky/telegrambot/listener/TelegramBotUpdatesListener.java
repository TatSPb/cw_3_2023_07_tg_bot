package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import com.pengrad.telegrambot.model.request.ParseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;
import pro.sky.telegrambot.service.TelegramBotService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.*;
import java.util.List;
import java.util.regex.*;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

//    private static final Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
        private static final Pattern PATTERN = Pattern.compile(
            "(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2})([A-zА-я}\\d;\\s!?:,'.]+)");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");

    private TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;
    private final TelegramBotService telegramBotService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskService notificationTaskService,
                                      TelegramBotService telegramBotService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
        this.telegramBotService = telegramBotService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Long chat_id = update.message().chat().id();
            Message message = update.message();
            String text = message.text();
            LocalDateTime dateTime;

            if (update.message() != null && text != null) {
                Matcher matcher = PATTERN.matcher(text);
                if (text.equals("/start")) {
                    telegramBotService.sendMessage(
                            chat_id,
//                            "Для планирования задачи отправьте её в формате: *01.01.2022 20:00 Сделать домашнюю работу*",
                            "To schedule a task send it in the next format: *01.01.2022 20:00 Homework to do*",
                            ParseMode.Markdown
                    );
                } else if (matcher.matches() && (dateTime = parse(matcher.group(1))) != null) {
                    notificationTaskService.save(chat_id, matcher.group(2), dateTime);
                    telegramBotService.sendMessage(chat_id, "Your task has been successfully scheduled");
                } else {
                    telegramBotService.sendMessage(chat_id, "Invalid message format");
                }
            } else {
                telegramBotService.sendMessage(
                        chat_id,
                        "Send the /start command or the other message to schedule a task"
                );
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Nullable
    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
