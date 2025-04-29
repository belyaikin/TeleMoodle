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
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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
    public String getBotUsername() {
        return "telemoodle_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // TODO: implement registration again

            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            TeleMoodleApplication.LOGGER.info("Received message: {}", message);

            showAvailableOptions(chatId);
        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update.getCallbackQuery());
        } else {
            sendRegularMessage(update.getMessage().getChatId(), "Please try again.");
        }
    }

    private void processCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        long chatIdCallback = callbackQuery.getMessage().getChatId();
        long userIdCallback = callbackQuery.getFrom().getId();

        String token = userService.getByTelegramId(userIdCallback).getMoodleToken();
        MoodleUser user = moodleService.getMoodleUser(token);

        if (callbackData.equals(CallbackType.SHOW_ALL_COURSES.callbackData)) {
            listAllCourses(chatIdCallback, token, user.getUserId());
        } else {
            sendRegularMessage(chatIdCallback, "Unknown callback query");
        }
    }

    private void listAllCourses(long chatId, String token, int userId) {
        List<MoodleCourse> courses = moodleService.getMoodleCourses(token, String.valueOf(userId));

        List<List<InlineKeyboardButton>> courseButtonsRows = new ArrayList<>();

        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Here are all your courses:");

        for (MoodleCourse course : courses) {
            List<InlineKeyboardButton> courseButtonsRow = new ArrayList<>();

            InlineKeyboardButton courseButton = new InlineKeyboardButton();
            courseButton.setText(course.getName());
            // temp
            courseButton.setCallbackData(CallbackType.SHOW_ALL_COURSES.callbackData);

            courseButtonsRow.add(courseButton);
            courseButtonsRows.add(courseButtonsRow);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(courseButtonsRows);
        msg.setReplyMarkup(markup);

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            TeleMoodleApplication.LOGGER.error("Something went wrong when showing listing all courses: {}", e.getMessage());
        }
    }

    private void showAvailableOptions(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Choose an option:");

        InlineKeyboardButton btn1 = new InlineKeyboardButton();
        btn1.setText("All Courses");
        btn1.setCallbackData(CallbackType.SHOW_ALL_COURSES.callbackData);

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
