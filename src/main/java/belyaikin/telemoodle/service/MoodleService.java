package belyaikin.telemoodle.service;

import belyaikin.telemoodle.TeleMoodleApplication;
import belyaikin.telemoodle.client.MoodleClient;
import belyaikin.telemoodle.model.moodle.*;
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

    public List<CourseInformation> getCourses(String token, String userId) {
        JSONArray array = new JSONArray(client.getUsersCourses(token, userId));

        List<CourseInformation> courses = new ArrayList<>();

        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);

                courses.add(new CourseInformation(
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

    public CourseInformation getCourseByID(String token, String courseId) {
        JSONObject courseJson = new JSONObject(client.getCourseByID(token, courseId))
                .getJSONArray("courses")
                .getJSONObject(0);

        try {
            return new CourseInformation(
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

    public List<CourseGrade> getCourseGrades(String token, String userId, String courseId) {
        JSONArray gradeItems = new JSONObject(client.getCourseGrades(token, userId, courseId))
                .getJSONArray("usergrades")
                .getJSONObject(0)
                .getJSONArray("gradeitems");

        List<CourseGrade> grades = new ArrayList<>();

        try {
            for (int i = 0; i < gradeItems.length(); i++) {
                JSONObject gradeJson = gradeItems.getJSONObject(i);

                grades.add(
                        new CourseGrade(
                                gradeJson.getInt("id"),
                                gradeJson.getString("itemname"),
                                gradeJson.isNull("graderaw") ? 0 : gradeJson.getLong("graderaw")
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

    public List<MoodleDeadline> getAllDeadlines(String token) {
        JSONObject deadlinesJson = new JSONObject(client.getAllDeadlines(token));
        List<MoodleDeadline> deadlines = new ArrayList<>();

        try {
            JSONArray events = deadlinesJson.getJSONArray("events");
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                MoodleDeadline deadline = new MoodleDeadline();
                deadline.setAssignmentName(event.getString("name"));
                deadline.setLastDay(event.getBoolean("islastday"));

                if (event.has("maxdaytimestamp")) {
                    deadline.setTimeEnd(event.getLong("timesort"));
                } else {
                    deadline.setTimeEnd(0);
                }

                JSONObject courseJson = event.getJSONObject("course");
                MoodleCourse course = new MoodleCourse();
                course.setId(courseJson.getInt("id"));
                course.setName(courseJson.getString("shortname"));
                deadline.setCourse(course);
                deadlines.add(deadline);
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
