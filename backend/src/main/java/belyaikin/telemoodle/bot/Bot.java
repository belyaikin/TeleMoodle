package belyaikin.telemoodle.bot;

import belyaikin.telemoodle.TeleMoodleApplication;
import belyaikin.telemoodle.model.User;
import belyaikin.telemoodle.model.moodle.MoodleUser;
import belyaikin.telemoodle.service.MoodleService;
import belyaikin.telemoodle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingBot {
    @Autowired private UserService userService;
    @Autowired private MoodleService moodleService;

    public Bot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();

        if (!userService.isUserRegistered(update.getMessage().getFrom().getId())) {
            // TODO: make better check
            if (message.length() < 32) {
                sendMessage(chatId,
                        "Please send me a valid Moodle security key."
                );
                return;
            }
            User user = new User();
            user.setTelegramId(userId);
            user.setMoodleToken(message);

            userService.create(user);

            sendMessage(chatId,
                    "Welcome to the MiniMoodle! Feel yourself like at home :)"
            );

            return;
        }

        String token = userService.getByTelegramId(userId).getMoodleToken();
        MoodleUser user = moodleService.getMoodleUser(token);

        sendMessage(chatId,
                "Hello, " + user.getFirstName() + "!"
        );
        sendMessage(chatId,
                "Your courses: " +
                        moodleService.getMoodleCourses(token, String.valueOf(user.getUserId()))
        );
    }

    @Override
    public String getBotUsername() {
        return "mini_moodle_bot";
    }

    public void sendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            TeleMoodleApplication.LOGGER.error("Error sending message: {}", e.getMessage());
        }
    }
}
