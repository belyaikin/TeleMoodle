package belyaikin.telemoodle.service;

import belyaikin.telemoodle.TeleMoodleApplication;
import belyaikin.telemoodle.client.MoodleClient;
import belyaikin.telemoodle.model.moodle.MoodleCourse;
import belyaikin.telemoodle.model.moodle.MoodleGrade;
import belyaikin.telemoodle.model.moodle.MoodleUser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Service
public class MoodleService {
    @Autowired
    private MoodleClient client;

    public MoodleUser getMoodleUser(String token) {
        JSONObject json = new JSONObject(client.getSiteInfo(token));
        MoodleUser user = new MoodleUser();

        user.setUserId(json.getInt("userid"));
        user.setFirstName(json.getString("firstname"));
        user.setLastName(json.getString("lastname"));

        return user;
    }

    public List<MoodleCourse> getCourses(String token, String userId) {
        JSONArray array = new JSONArray(client.getUsersCourses(token, userId));

        List<MoodleCourse> courses = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            MoodleCourse course = new MoodleCourse();

            course.setId(jsonObject.getInt("id"));
            course.setName(jsonObject.getString("shortname"));

            courses.add(course);
        }

        return courses;
    }

    public String getCourseByID(String token, String userId, String courseId) {
        JSONObject courseJson = new JSONObject(client.getCourseByID(token, courseId))
                .getJSONArray("courses")
                .getJSONObject(0);

        MoodleCourse course = new MoodleCourse();
        course.setId(courseJson.getInt("id"));
        course.setName(courseJson.getString("shortname"));
        course.setGrades(getCourseGrades(token, userId, String.valueOf(course.getId())));

        MoodleUser student = getMoodleUser(token);

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

        return res.toString();
    }


    private List<MoodleGrade> getCourseGrades(String token, String userId, String courseId) {
        JSONArray gradeItems = new JSONObject(client.getCourseGrades(token, userId, courseId))
                .getJSONArray("usergrades")
                .getJSONObject(0)
                .getJSONArray("gradeitems");

        TeleMoodleApplication.LOGGER.info(String.valueOf(gradeItems));

        List<MoodleGrade> grades = new ArrayList<>();

        for (int i = 0; i < gradeItems.length(); i++) {
            MoodleGrade grade = new MoodleGrade();
            JSONObject gradeJson = gradeItems.getJSONObject(i);

            grade.setId(gradeJson.getInt("id"));
            grade.setName(gradeJson.getString("itemname"));

            if (!gradeJson.isNull("graderaw")) {
                grade.setRaw(gradeJson.getLong("graderaw"));
            }
            else {
                grade.setRaw(0);
            }

            grades.add(grade);
        }

        return grades;
    }
}
