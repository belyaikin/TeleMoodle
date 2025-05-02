package belyaikin.telemoodle.bot;

import belyaikin.telemoodle.TeleMoodleApplication;
import belyaikin.telemoodle.model.User;
import belyaikin.telemoodle.model.moodle.MoodleCourse;
import belyaikin.telemoodle.model.moodle.MoodleDeadline;
import belyaikin.telemoodle.model.moodle.MoodleGrade;
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
            // TODO: implement registration again

            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();

            TeleMoodleApplication.LOGGER.info("Received message: {}", message);

            if(!userService.isUserRegistered(userId)){
                if(message.length() != 32) {
                    sendRegularMessage(chatId, "Welcome to Puddle \nPlease send me a valid Moodle security key");
                    return;
                }

                User user = new User();
                user.setTelegramId(userId);
                user.setMoodleToken(message);

                userService.create(user);

                sendRegularMessage(chatId, "You were registered successful");

                return;

            }


            if (message.equals("/showcourses")) {
                showAvailableOptions(chatId);
            }

            if(message.equals("/showdeadlines")){
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
                            .append("Is Last Day: ").append(deadline.getIsLastDay()).append("\n");
                }

                sendRegularMessage(chatId, messageText.toString());
                return;
            }

            sendRegularMessage(chatId, "Please try send an available command");

        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update.getCallbackQuery());
        } else {
            sendRegularMessage(update.getMessage().getChatId(), "Please try send an available command.");
        }
    }

    private void processCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        long chatIdCallback = callbackQuery.getMessage().getChatId();
        long userIdCallback = callbackQuery.getFrom().getId();

        String token = userService.getByTelegramId(userIdCallback).getMoodleToken();
        MoodleUser user = moodleService.getMoodleUser(token);

        TeleMoodleApplication.LOGGER.info("User ID: " + user.getUserId());

        if (callbackData.equals("all_courses")) {
            listAllCourses(chatIdCallback, token, user.getUserId());
        } else {
            MoodleCourse course = moodleService.getCourseByID(token, String.valueOf(user.getUserId()), callbackData);

            MoodleUser student = moodleService.getMoodleUser(token);

            StringBuilder res = new StringBuilder();
            StringBuilder registerGrades = new StringBuilder();
            StringBuilder termGrades = new StringBuilder();
            StringBuilder otherGrades = new StringBuilder();
            String attendance = "";
            boolean hasGrades = false;

            res.append("Student: \n").append(student.getFirstName() + " ").append(student.getLastName() + "\n\n");

            res.append("Course name:\n").append(course.getName()).append("\n\n");

            for (MoodleGrade grade : course.getGrades()) {
                String name = grade.getName();
                long raw = grade.getRaw();

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
        List<MoodleCourse> courses = moodleService.getCourses(token, String.valueOf(userId));

        List<List<InlineKeyboardButton>> courseButtonsRows = new ArrayList<>();

        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Here are all your courses:");

        for (MoodleCourse course : courses) {
            List<InlineKeyboardButton> courseButtonsRow = new ArrayList<>();

            InlineKeyboardButton courseButton = new InlineKeyboardButton();
            courseButton.setText(course.getName());
            // temp
            courseButton.setCallbackData(String.valueOf(course.getId()));

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
