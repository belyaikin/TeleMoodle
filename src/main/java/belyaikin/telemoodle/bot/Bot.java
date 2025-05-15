package belyaikin.telemoodle.bot;

import belyaikin.telemoodle.TeleMoodleApplication;
import belyaikin.telemoodle.model.User;
import belyaikin.telemoodle.model.moodle.*;
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

            if (!userService.isUserRegistered(userId)) {
                startRegistrationProcess(message, chatId, userId);
                return;
            }

            switch (message) {
                case "/courses":
                    showAvailableCourseOptions(chatId);
                    break;
                case "/deadlines":
                    showDeadlines(chatId, userId);
                    break;
                default:
                    if (!userService.isUserRegistered(userId)) {
                        startRegistrationProcess(message, chatId, userId);
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

    private void startRegistrationProcess(String message, long chatId, long userId) {
        sendRegularMessage(chatId,
                """
                        Hello! 👋
                        It looks like you are texting me for the first time.
                        
                        Please send me your Moodle token, so I can get information from Moodle.
                        
                        ReMoodle team has written a pretty clear instruction of how to obtain it.
                        https://ext.remoodle.app/find-token"""
        );

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
        long userIdCallback = callbackQuery.getFrom().getId();

        String token = userService.getByTelegramId(userIdCallback).getMoodleToken();
        MoodleUser user = moodleService.getMoodleUser(token);

        if (callbackData.equals("all_courses")) {
            listAllCourses(chatIdCallback, token, user.getUserId());
        } else {
            // If callback data equals course id. Refactor this!
            CourseInformation course = moodleService.getCourseByID(token, callbackData);

            MoodleUser student = moodleService.getMoodleUser(token);

            StringBuilder res = new StringBuilder();
            StringBuilder registerGrades = new StringBuilder();
            StringBuilder termGrades = new StringBuilder();
            StringBuilder otherGrades = new StringBuilder();
            String attendance = "";
            boolean hasGrades = false;

            res.append("Student: \n").append(student.getFirstName()).append(" ").append(student.getLastName()).append("\n\n");

            res.append("Course name:\n").append(course.name()).append("\n\n");

            for (CourseGrade grade : moodleService.getCourseGrades(token, String.valueOf(user.getUserId()), String.valueOf(course.id()))) {
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

            sendRegularMessage(chatIdCallback, String.valueOf(res));
        }
    }

    private void listAllCourses(long chatId, String token, int userId) {
        List<CourseInformation> courses = moodleService.getCourses(token, String.valueOf(userId));

        List<List<InlineKeyboardButton>> courseButtonsRows = new ArrayList<>();

        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Here are all your courses:");

        for (CourseInformation course : courses) {
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
        List<MoodleDeadline> deadlines = moodleService.getAllDeadlines(token);

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Yekaterinburg"));

        StringBuilder messageText = new StringBuilder();
        messageText.append("Here are your upcoming deadlines:\n\n");

        SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss", Locale.ENGLISH);

        for (MoodleDeadline deadline : deadlines) {
            if (deadline.getAssignmentName().contains("Attendance")) continue;
            long timestampMillis = deadline.getTimeEnd() * 1000L;
            Date date = new Date(timestampMillis);
            String formattedDate = sdf.format(date);
            messageText
                    .append("-----------------").append("\n")
                    .append("Course: ").append(deadline.getCourse().getName()).append("  ||  ")
                    .append(deadline.getAssignmentName()).append("  ||  ")
                    .append("Due Date: ").append(formattedDate).append("  ||  ")
                    .append("Is Last Day: ").append(deadline.isLastDay() ? "Yes" : "No").append("\n");
        }

        sendRegularMessage(chatId, messageText.toString());
    }

}
