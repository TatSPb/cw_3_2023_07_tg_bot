package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;

@Service
public class TelegramBotService {
    private Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
    @Autowired
    private final TelegramBot telegramBot;

    public TelegramBotService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void sendMessage(long chat_id, String text, @Nullable ParseMode parseMode) {
        SendMessage sendMessage = new SendMessage(chat_id, text);
        if (parseMode != null) {
            sendMessage.parseMode(parseMode);
        }
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error("SendMessage was failed due to: " + sendResponse.description());
        }
    }

    public void sendMessage(long chatId, String text) {
        sendMessage(chatId, text, null);
    }


}
