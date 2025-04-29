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

    public MoodleCourse getCourseByID(String token, String userId, String courseId) {
        JSONObject courseJson = new JSONObject(client.getCourseByID(token, courseId))
                .getJSONArray("courses")
                .getJSONObject(0);

        MoodleCourse course = new MoodleCourse();
        course.setId(courseJson.getInt("id"));
        course.setName(courseJson.getString("shortname"));
        course.setGrades(getCourseGrades(token, userId, String.valueOf(course.getId())));

        return course;
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
