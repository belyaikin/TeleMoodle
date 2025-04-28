package belyaikin.telemoodle.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OkHttpClientConfiguration {
    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient();
    }
}
