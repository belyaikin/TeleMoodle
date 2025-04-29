package belyaikin.telemoodle.service;

import belyaikin.telemoodle.client.MoodleClient;
import belyaikin.telemoodle.model.moodle.MoodleCourse;
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

    public MoodleCourse getMoodleCourseById(String token, int courseId) {
        String response = client.getCourseContent(token,courseId);
        if(!response.trim().startsWith("[")){
            throw new RuntimeException("Unexpected response: " + response);
        }

        JSONArray json = new JSONArray(client.getCourseContent(token, courseId));
        MoodleCourse course = new MoodleCourse();
        course.setId(courseId);

        List<String> moduleNames = new ArrayList<>();

        for(int i = 0; i < json.length(); i++){
            JSONObject section = json.getJSONObject(i);

            if(!section.has("modules")) continue;

            JSONArray modules = section.getJSONArray("modules");

            for(int j = 0; j < modules.length(); j++){
                JSONObject module = modules.getJSONObject(j);
                moduleNames.add(module.getString("name"));
            }

        }

        course.setModuleNames(moduleNames);

        return course;

    }

    public List<MoodleCourse> getMoodleCourses(String token, String userid) {
        JSONArray array = new JSONArray(client.getUsersCourses(token, userid));

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
}
