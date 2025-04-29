package belyaikin.telemoodle.bot;

import belyaikin.telemoodle.TeleMoodleApplication;
import belyaikin.telemoodle.model.moodle.MoodleCourse;
import belyaikin.telemoodle.model.moodle.MoodleUser;
import belyaikin.telemoodle.service.MoodleService;
import belyaikin.telemoodle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot {
    @Autowired private UserService userService;
    @Autowired private MoodleService moodleService;


    public Bot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            TeleMoodleApplication.LOGGER.info("Received message: {}", message);

            if (message.equals("/start")) {
                showAvailableOptions(chatId);
            }

        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatIdCallback = update.getCallbackQuery().getMessage().getChatId();

            TeleMoodleApplication.LOGGER.info("Received callback: {}", callbackData);

            if (callbackData.equals(CallbackType.SHOW_ALL_COURSES.string)) {
                long userIdCallback = update.getCallbackQuery().getFrom().getId();
                String token = userService.getByTelegramId(userIdCallback).getMoodleToken();
                MoodleUser user = moodleService.getMoodleUser(token);

                List<MoodleCourse> courses = moodleService.getMoodleCourses(token, String.valueOf(user.getUserId()));

                StringBuilder coursesList = new StringBuilder("Here are all your courses:\n\n");
                for (int i = 0; i < courses.size(); i++) {
                    MoodleCourse course = courses.get(i);
                    coursesList.append(i + 1).append(". ").append(course.getName()).append("\n");
                }

                sendRegularMessage(chatIdCallback, coursesList.toString());
            }
        } else {
            sendRegularMessage(update.getMessage().getChatId(), "Please try again.");
        }
    }

    @Override
    public String getBotUsername() {
        return "telemoodle_bot";
    }

    private void showAvailableOptions(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Choose an option:");

        InlineKeyboardButton btn1 = new InlineKeyboardButton();
        btn1.setText("All Courses");
        btn1.setCallbackData("all_courses");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(btn1);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        msg.setReplyMarkup(markup);

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            TeleMoodleApplication.LOGGER.error("Something went wrong when showing available options: {}", e.getMessage());
        }
    }

    private void sendRegularMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            TeleMoodleApplication.LOGGER.error("Error sending message: {}", e.getMessage());
        }
    }
}
