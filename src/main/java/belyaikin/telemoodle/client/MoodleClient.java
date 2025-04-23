package belyaikin.telemoodle.client;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MoodleClient {
    @Autowired
    private OkHttpClient client;

    @Value("${moodle.url}")
    private String moodleUrl;

    public String getSiteInfo(String token) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(moodleUrl)
                .addPathSegment("webservice")
                .addPathSegment("rest")
                .addPathSegment("server.php")
                .addQueryParameter("wstoken", token)
                .addQueryParameter("wsfunction", "core_webservice_get_site_info")
                .addQueryParameter("moodlewsrestformat", "json")
                .build();

        var request = new Request.Builder()
                .url(url)
                .build();

        try (var response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsersCourses(String token, String userid) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(moodleUrl)
                .addPathSegment("webservice")
                .addPathSegment("rest")
                .addPathSegment("server.php")
                .addQueryParameter("wstoken", token)
                .addQueryParameter("wsfunction", "core_enrol_get_users_courses")
                .addQueryParameter("userid", userid)
                .addQueryParameter("moodlewsrestformat", "json")
                .build();

        var request = new Request.Builder()
                .url(url)
                .build();

        try (var da = client.newCall(request).execute()) {
            return da.body() != null ? da.body().string() : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
