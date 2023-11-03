package pro.sky.telegrambot;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import com.pengrad.telegrambot.response.SendResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.service.NotificationTaskService;
import pro.sky.telegrambot.service.TelegramBotService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
public class TelegramBotUpdatesListenerTest {

    private final TelegramBot telegramBot = Mockito.mock(TelegramBot.class);
    private final NotificationTaskService notificationTaskService = Mockito.mock(NotificationTaskService.class);

    @InjectMocks
    private TelegramBotUpdatesListener telegramBotUpdatesListener = new TelegramBotUpdatesListener(
            telegramBot,
            notificationTaskService,
            new TelegramBotService(telegramBot)
    );

    @BeforeEach
    public void beforeEach() {
        Mockito.when(telegramBot.execute(any())).thenReturn(
                BotUtils.fromJson(
                        """
                                {
                                    "ok": true
                                }
                                """,
                        SendResponse.class
                )
        );
    }

    @Test
    public void handleStartTest() throws URISyntaxException, IOException {
//        String json = Files.readString(
//                Paths.get(TelegramBotUpdatesListener.class.getResource("text_update.json").toURI()));
        Path path = Paths.get("src/test/resources/pro/sky/telegrambot/text_update.json");
        String json = Files.readString(path);
        Update update = getUpdate(json, "/start");
        telegramBotUpdatesListener.process(Collections.singletonList(update));

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot).execute(argumentCaptor.capture());
        SendMessage actual = argumentCaptor.getValue();

        Assertions.assertThat(actual.getParameters().get("chat_id")).isEqualTo(123L);
        Assertions.assertThat(actual.getParameters().get("text")).isEqualTo(
                "To schedule a task send it in the next format: *01.01.2022 20:00 Homework to do*");
        Assertions.assertThat(actual.getParameters().get("parse_mode")).isEqualTo(ParseMode.Markdown.name());
    }

    @Test
    public void handleInvalidMessage() throws URISyntaxException, IOException {
//        String json = Files.readString(
//                Paths.get(TelegramBotUpdatesListener.class.getResource("telegrambot/text_update.json").toURI()));
        Path path = Paths.get("src/test/resources/pro/sky/telegrambot/text_update.json");
        String json = Files.readString(path);

        Update update = getUpdate(json, "task");
        telegramBotUpdatesListener.process(Collections.singletonList(update));

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot).execute(argumentCaptor.capture());
        SendMessage actual = argumentCaptor.getValue();

        Assertions.assertThat(actual.getParameters().get("chat_id")).isEqualTo(123L);
        Assertions.assertThat(actual.getParameters().get("text")).isEqualTo("Invalid message format");
    }

    @Test
    public void handleInvalidDateFormat() throws URISyntaxException, IOException {
        Path path = Paths.get("src/test/resources/pro/sky/telegrambot/text_update.json");
        String json = Files.readString(path);
        //Paths.get(TelegramBotUpdatesListener.class.getResource("text_update.json").toURI()));

        Update update = getUpdate(json, "32.11.2023 20:00 task");
        telegramBotUpdatesListener.process(Collections.singletonList(update));

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot).execute(argumentCaptor.capture());
        SendMessage actual = argumentCaptor.getValue();

        Assertions.assertThat(actual.getParameters().get("chat_id")).isEqualTo(123L);
        Assertions.assertThat(actual.getParameters().get("text")).isEqualTo("Invalid message format");
    }

    @Test
    public void handleValidMessage() throws URISyntaxException, IOException {
        Path path = Paths.get("src/test/resources/pro/sky/telegrambot/text_update.json");
        String json = Files.readString(path);
        //Paths.get(TelegramBotUpdatesListener.class.getResource("text_update.json").toURI()));

        Update update = getUpdate(json, "03.11.2023 20:00 task");
        telegramBotUpdatesListener.process(Collections.singletonList(update));

        ArgumentCaptor<SendMessage> sendMessageArgumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot).execute(sendMessageArgumentCaptor.capture());
        SendMessage actualSendMessage = sendMessageArgumentCaptor.getValue();

        ArgumentCaptor<LocalDateTime> localDateTimeArgumentCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<String> stringTimeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(notificationTaskService).save(
                longArgumentCaptor.capture(),
                stringTimeArgumentCaptor.capture(),
                localDateTimeArgumentCaptor.capture()
        );

        LocalDateTime actualLocalDateTime = localDateTimeArgumentCaptor.getValue();
        String actualString = stringTimeArgumentCaptor.getValue();
        Long actualLong = longArgumentCaptor.getValue();

        Assertions.assertThat(actualLocalDateTime)
                .isEqualTo(LocalDateTime.of(2023, Month.NOVEMBER, 03, 20, 00));
        Assertions.assertThat(actualString).isEqualTo(" task");
        Assertions.assertThat(actualLong).isEqualTo(123L);

        Assertions.assertThat(actualSendMessage.getParameters().get("chat_id")).isEqualTo(123L);
        Assertions.assertThat(actualSendMessage.getParameters().get("text")).isEqualTo("Your task has been successfully scheduled");

    }

    private Update getUpdate(String json, String replaced) {
        return BotUtils.fromJson(json.replace("%text%", replaced), Update.class);
    }


}
