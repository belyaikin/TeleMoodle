package belyaikin.telemoodle.client;

import belyaikin.telemoodle.TeleMoodleApplication;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

    public String getUsersCourses(String token, String userId) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(moodleUrl)
                .addPathSegment("webservice")
                .addPathSegment("rest")
                .addPathSegment("server.php")
                .addQueryParameter("wstoken", token)
                .addQueryParameter("wsfunction", "core_enrol_get_users_courses")
                .addQueryParameter("userid", userId)
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

    public String getAllDeadlines(String token) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(moodleUrl)
                .addPathSegment("webservice")
                .addPathSegment("rest")
                .addPathSegment("server.php")
                .addQueryParameter("wstoken", token)
                .addQueryParameter("wsfunction", "core_calendar_get_calendar_upcoming_view")
                .addQueryParameter("moodlewsrestformat", "json")
                .build();

        var request = new Request.Builder()
                .url(url)
                .build();

        try (var response = client.newCall(request).execute()){
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getCourseByID(String token, String courseId) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(moodleUrl)
                .addPathSegment("webservice")
                .addPathSegment("rest")
                .addPathSegment("server.php")
                .addQueryParameter("wstoken", token)
                .addQueryParameter("wsfunction", "core_course_get_courses_by_field")
                .addQueryParameter("field", "id")
                .addQueryParameter("value", courseId)
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

    public String getCourseGrades(String token, String userId, String courseId) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(moodleUrl)
                .addPathSegment("webservice")
                .addPathSegment("rest")
                .addPathSegment("server.php")
                .addQueryParameter("wstoken", token)
                .addQueryParameter("wsfunction", "gradereport_user_get_grade_items")
                .addQueryParameter("userid", userId)
                .addQueryParameter("courseid", courseId)
                .addQueryParameter("moodlewsrestformat", "json")
                .build();

        var request = new Request.Builder()
                .url(url)
                .build();
        TeleMoodleApplication.LOGGER.info("URL: " + url);

        try (var response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
