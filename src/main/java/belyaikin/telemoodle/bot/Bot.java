package belyaikin.telemoodle.bot;

import belyaikin.telemoodle.TeleMoodleApplication;
import belyaikin.telemoodle.model.User;
import belyaikin.telemoodle.model.moodle.*;
import belyaikin.telemoodle.model.moodle.course.Deadline;
import belyaikin.telemoodle.model.moodle.course.Grade;
import belyaikin.telemoodle.model.moodle.course.Course;
import belyaikin.telemoodle.service.MoodleService;
import belyaikin.telemoodle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class Bot extends TelegramLongPollingBot {
    @Autowired private UserService userService;
    @Autowired private MoodleService moodleService;

    @Autowired private MessageHandler messageHandler;

    public Bot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return "telemoodle_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (update.getMessage().getText().startsWith("/")) {
                sendMessage(messageHandler.handleCommand(update));
            }
            else {
                sendMessage(messageHandler.handleText(update));
            }
        }
    }

    private void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            TeleMoodleApplication.LOGGER.error("Error sending message: {}", e.getMessage());
        }
    }
}
