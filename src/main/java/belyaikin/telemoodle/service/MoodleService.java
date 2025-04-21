package belyaikin.telemoodle.service;

import belyaikin.telemoodle.client.MoodleClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MoodleService {
    @Autowired
    private MoodleClient client;

    public int getUserId(String token) {
        return parse(client.getSiteInfo(token)).getInt("userid");
    }

    private static JSONObject parse(String jsonString) {
        return new JSONObject(jsonString);
    }
}
