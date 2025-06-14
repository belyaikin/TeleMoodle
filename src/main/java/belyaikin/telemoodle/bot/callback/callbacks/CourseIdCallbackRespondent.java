package belyaikin.telemoodle.bot.callback.callbacks;

import belyaikin.telemoodle.bot.callback.CallbackRespondent;
import belyaikin.telemoodle.model.User;
import belyaikin.telemoodle.model.moodle.MoodleUser;
import belyaikin.telemoodle.model.moodle.course.Course;
import belyaikin.telemoodle.model.moodle.course.Grade;
import belyaikin.telemoodle.service.MoodleService;
import belyaikin.telemoodle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CourseIdCallbackRespondent implements CallbackRespondent {
    @Autowired private UserService userService;
    @Autowired private MoodleService moodleService;

    @Override
    public SendMessage execute(String data, Update update) {
        User user = userService.getByTelegramId(update.getMessage().getFrom().getId());
        MoodleUser moodleUser = moodleService.getMoodleUser(user.getMoodleToken());

        Course course = moodleService.getCourseByID(user.getMoodleToken(), data);

        MoodleUser student = moodleService.getMoodleUser(user.getMoodleToken());

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

        for (Grade grade : moodleService.getCourseGrades(user.getMoodleToken(), String.valueOf(moodleUser.getUserId()), String.valueOf(course.id()))) {
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

        return new SendMessage(String.valueOf(update.getMessage().getChatId()), res.toString());
    }
}
