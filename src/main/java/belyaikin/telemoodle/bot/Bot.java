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
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();

            switch (message) {
                case "/start":
                    if (!userService.isUserRegistered(userId)) {
                        sendRegularMessage(chatId,
                                """
                                        Hello! 👋
                                        It looks like you are texting me for the first time.
                                        
                                        Please send me your Moodle token, so I can get information from Moodle.
                                        
                                        ReMoodle team has written a pretty clear instruction of how to obtain it.
                                        https://ext.remoodle.app/find-token"""
                        );
                    } else {
                        sendRegularMessage(chatId,
                                "Click the menu button near the text input field to show available commands 😊"
                        );
                    }
                    break;
                case "/courses":
                    if (!userService.isUserRegistered(userId)) {
                        sendRegularMessage(chatId,
                                "❌ You are not registered yet. Please send me a Moodle key."
                        );
                    }
                    showAvailableCourseOptions(chatId);
                    break;
                case "/deadlines":
                    if (!userService.isUserRegistered(userId)) {
                        sendRegularMessage(chatId,
                                "❌ You are not registered yet. Please send me a Moodle key."
                        );
                    }
                    showDeadlines(chatId, userId);
                    break;
                default:
                    if (!userService.isUserRegistered(userId)) {
                        checkToken(message, chatId, userId);
                    } else {
                        sendRegularMessage(chatId,
                                "Click the menu button near the text input field to show available commands 😊"
                        );
                    }
                    break;
            }

        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update.getCallbackQuery());
        } else {
            sendRegularMessage(update.getMessage().getChatId(), "Something is wrong, I can feel it...");
        }
    }

    private void checkToken(String message, long chatId, long userId) {
        if(message.length() != 32) {
            sendRegularMessage(chatId, "❌ I think this token is invalid! Please send me a valid Moodle key.");
            return;
        }

        User user = new User();
        user.setTelegramId(userId);
        user.setMoodleToken(message);

        userService.create(user);

        sendRegularMessage(chatId, """
                        ✅ You were registered successfully! Welcome to TeleMoodle, feel yourself like at home 😊
                        Click the menu button near the text input field to show available commands."""
        );
    }

    private void processCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        long chatIdCallback = callbackQuery.getMessage().getChatId();
        int messageIdCallback = callbackQuery.getMessage().getMessageId();
        long userIdCallback = callbackQuery.getFrom().getId();

        String token = userService.getByTelegramId(userIdCallback).getMoodleToken();
        MoodleUser user = moodleService.getMoodleUser(token);

        if (callbackData.equals("all_courses")) {
            listAllCourses(chatIdCallback, messageIdCallback, token, user.getUserId());
        } else {
            // If callback data equals course id. Refactor this!
            Course course = moodleService.getCourseByID(token, callbackData);

            MoodleUser student = moodleService.getMoodleUser(token);

            StringBuilder res = new StringBuilder();
            StringBuilder registerGrades = new StringBuilder();
            StringBuilder termGrades = new StringBuilder();
            StringBuilder otherGrades = new StringBuilder();
            String attendance = "";
            boolean hasGrades = false;

            res.append("Student: \n").append(student.getFirstName()).append(" ").append(student.getLastName()).append("\n\n");

            res.append("Course:\n").append(course.name())
                    .append("\n")
                    .append("Teacher: ").append(course.teacher())
                    .append("\n\n");

            for (Grade grade : moodleService.getCourseGrades(token, String.valueOf(user.getUserId()), String.valueOf(course.id()))) {
                String name = grade.name();
                long raw = grade.raw();

                if (name == null || name.isBlank()) continue;

                if (name.equalsIgnoreCase("Attendance")) {
                    attendance = "Course attendance: " + raw + "%\n";
                    continue;
                }

                hasGrades = true;

                if (name.contains("Register")) {
                    registerGrades.append(name).append(": ").append(raw).append("\n");
                } else if (name.matches("(?i).*Midterm.*|.*Endterm.*|.*Final.*|.*Term.*")) {
                    termGrades.append(name).append(": ").append(raw).append("\n");
                } else {
                    otherGrades.append(name).append(": ").append(raw).append("\n");
                }
            }

            if (!attendance.isEmpty()) res.append(attendance).append("\n");

            if (hasGrades) {
                res.append("Course grades:\n");
                if (!registerGrades.isEmpty()) res.append("\nRegisters:\n").append(registerGrades);
                if (!termGrades.isEmpty()) res.append("\nTerm Grades:\n").append(termGrades);
                if (!otherGrades.isEmpty()) res.append("\nOther Grades:\n").append(otherGrades);
            } else {
                res.append("No grades available.");
            }

            changeMessage(chatIdCallback, messageIdCallback, res.toString());
        }
    }

    private void changeMessage(long chatId, int messageId, String newText) {
        EditMessageText newMessage = new EditMessageText();

        newMessage.setChatId(chatId);
        newMessage.setMessageId(messageId);
        newMessage.setText(newText);

        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            TeleMoodleApplication.LOGGER.error("Something went wrong when editing a message: {}", e.getMessage());;
        }
    }

    private void listAllCourses(long chatId, int messageId, String token, int userId) {
        List<Course> courses = moodleService.getCourses(token, String.valueOf(userId));

        List<List<InlineKeyboardButton>> courseButtonsRows = new ArrayList<>();

        EditMessageText msg = new EditMessageText();
        msg.setChatId(String.valueOf(chatId));
        msg.setMessageId(messageId);
        msg.setText("Here are all your courses:");

        for (Course course : courses) {
            List<InlineKeyboardButton> courseButtonsRow = new ArrayList<>();

            InlineKeyboardButton courseButton = new InlineKeyboardButton();
            courseButton.setText(course.name());
            courseButton.setCallbackData(String.valueOf(course.id()));

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

    private void showAvailableCourseOptions(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Choose an option:");

        InlineKeyboardButton allCourses = new InlineKeyboardButton();
        allCourses.setText("All Courses");
        allCourses.setCallbackData("all_courses");

        InlineKeyboardButton currentCourses = new InlineKeyboardButton();
        currentCourses.setText("Current Courses");
        currentCourses.setCallbackData("all_courses");

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        row.add(allCourses);
        row.add(currentCourses);
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

    private void showDeadlines(long chatId, long userId) {
        String token = userService.getByTelegramId(userId).getMoodleToken();
        List<Deadline> deadlines = moodleService.getAllDeadlines(token);

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+5:00"));

        StringBuilder messageText = new StringBuilder();
        messageText.append("\uD83D\uDDD3 Here are your upcoming deadlines:\n\n");

        SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss", Locale.ENGLISH);

        for (Deadline deadline : deadlines) {
            if (deadline.name().contains("Attendance")) continue;

            long timestampMillis = deadline.timeEnd() * 1000L;
            Date date = new Date(timestampMillis);
            String formattedDate = sdf.format(date);

            messageText
                    .append(deadline.lastDay() ? "❗⏰ " : "⏰ ")
                    .append(deadline.name()).append(" (").append(deadline.course().name()).append(") ")
                    .append("is until ").append(formattedDate).append("\n\n");
        }

        sendRegularMessage(chatId, messageText.toString());
    }
}
