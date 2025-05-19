package belyaikin.telemoodle.bot.command;

import belyaikin.telemoodle.bot.lang.Messages;
import belyaikin.telemoodle.model.User;
import belyaikin.telemoodle.model.moodle.MoodleUser;
import belyaikin.telemoodle.model.moodle.course.Course;
import belyaikin.telemoodle.service.MoodleService;
import belyaikin.telemoodle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class CoursesCommand implements Command {
    @Autowired private UserService userService;
    @Autowired private MoodleService moodleService;

    @Override
    public SendMessage execute(Update update) {
        User user = userService.getByTelegramId(update.getMessage().getFrom().getId());
        MoodleUser moodleUser = moodleService.getMoodleUser(user.getMoodleToken());

        List<Course> courses = moodleService.getCourses(user.getMoodleToken(), String.valueOf(moodleUser.getUserId()));

        List<List<InlineKeyboardButton>> courseButtonsRows = new ArrayList<>();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(Messages.HERE_IS_ALL_YOUR_COURSES);

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
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }
}
