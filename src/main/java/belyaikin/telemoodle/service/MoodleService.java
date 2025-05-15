package belyaikin.telemoodle.service;

import belyaikin.telemoodle.TeleMoodleApplication;
import belyaikin.telemoodle.client.MoodleClient;
import belyaikin.telemoodle.model.moodle.*;
import belyaikin.telemoodle.model.moodle.course.Deadline;
import belyaikin.telemoodle.model.moodle.course.Grade;
import belyaikin.telemoodle.model.moodle.course.Course;
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

        try {
            user.setUserId(json.getInt("userid"));
            user.setFirstName(json.getString("firstname"));
            user.setLastName(json.getString("lastname"));
        } catch (Exception e) {
            TeleMoodleApplication.LOGGER.error("""
                    Something went wrong when a Moodle user! Here's some info about it:
                    JSON returned from Moodle: {},
                    exception message: {}""",
                    json, e.getMessage());
        }

        return user;
    }

    public List<Course> getCourses(String token, String userId) {
        JSONArray array = new JSONArray(client.getUsersCourses(token, userId));

        List<Course> courses = new ArrayList<>();

        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);

                courses.add(new Course(
                        jsonObject.getInt("id"),
                        jsonObject.getString("displayname"),
                        jsonObject.getLong("startdate"),
                        jsonObject.getLong("enddate")
                ));
            }
        } catch (Exception e) {
            TeleMoodleApplication.LOGGER.error("""
                    Something went wrong when getting courses! Here's some info about it:
                    JSON returned from Moodle: {},
                    exception message: {}""",
                    array, e.getMessage());
        }

        return courses;
    }

    public Course getCourseByID(String token, String courseId) {
        JSONObject courseJson = new JSONObject(client.getCourseByID(token, courseId))
                .getJSONArray("courses")
                .getJSONObject(0);

        try {
            return new Course(
                    courseJson.getInt("id"),
                    courseJson.getString("displayname"),
                    courseJson.getLong("startdate"),
                    courseJson.getLong("enddate")
            );
        } catch (Exception e) {
            TeleMoodleApplication.LOGGER.error("""
                    Something went wrong when getting course by ID! Here's some info about it:
                    JSON returned from Moodle: {},
                    exception message: {}""",
                    courseJson, e.getMessage());
        }

        return null;
    }

    public List<Grade> getCourseGrades(String token, String userId, String courseId) {
        JSONObject userGradesObject = new JSONObject(
                client.getCourseGrades(token, userId, courseId)).getJSONArray("usergrades").getJSONObject(0
        );

        JSONArray gradeItems = userGradesObject.getJSONArray("gradeitems");

        List<Grade> grades = new ArrayList<>();

        try {
            for (int i = 0; i < gradeItems.length(); i++) {
                JSONObject gradeJson = gradeItems.getJSONObject(i);

                grades.add(
                        new Grade(
                                gradeJson.getInt("id"),
                                gradeJson.getString("itemname"),
                                gradeJson.isNull("graderaw") ? 0 : gradeJson.getLong("graderaw"),
                                getCourseByID(token, String.valueOf(userGradesObject.getInt("courseid")))
                        )
                );
            }
        } catch (Exception e) {
            TeleMoodleApplication.LOGGER.error("""
                    Something went wrong when getting course grades! Here's some info about it:
                    JSON returned from Moodle: {},
                    exception message: {}""",
                    gradeItems, e.getMessage());
        }

        return grades;
    }

    public List<Deadline> getAllDeadlines(String token) {
        JSONObject deadlinesJson = new JSONObject(client.getAllDeadlines(token));
        List<Deadline> deadlines = new ArrayList<>();

        try {
            JSONArray events = deadlinesJson.getJSONArray("events");

            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);

                // Why moodle includes a whole course object along with an event object???
                JSONObject course = event.getJSONObject("course");

                if (!event.has("maxdaytimestamp")) continue;

                deadlines.add(
                        new Deadline(
                                event.getString("name"),
                                event.getLong("timesort"),
                                event.getBoolean("islastday"),
                                new Course(
                                        course.getInt("id"),
                                        course.getString("fullnamedisplay"),
                                        course.getLong("startdate"),
                                        course.getLong("enddate")
                                )
                        )
                );
            }
        } catch (Exception e) {
            TeleMoodleApplication.LOGGER.error("""
                   Something went wrong when getting deadlines! Here's some info about it:
                   JSON returned from Moodle: {},
                   exception message: {}""",
                    deadlinesJson, e.getMessage());
        }

        return deadlines;
    }
}
