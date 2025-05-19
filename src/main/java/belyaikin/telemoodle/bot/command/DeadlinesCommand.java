package belyaikin.telemoodle.bot.command;

import belyaikin.telemoodle.model.User;
import belyaikin.telemoodle.model.moodle.MoodleUser;
import belyaikin.telemoodle.model.moodle.course.Deadline;
import belyaikin.telemoodle.service.MoodleService;
import belyaikin.telemoodle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Component
public class DeadlinesCommand implements Command {
    @Autowired private UserService userService;
    @Autowired private MoodleService moodleService;

    @Override
    public SendMessage execute(Update update) {
        User user = userService.getByTelegramId(update.getMessage().getFrom().getId());

        List<Deadline> deadlines = moodleService.getAllDeadlines(user.getMoodleToken());

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

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(messageText.toString());

        return sendMessage;
    }
}
