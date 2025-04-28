package belyaikin.telemoodle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TeleMoodleApplication {
	public static final Logger LOGGER = LoggerFactory.getLogger(TeleMoodleApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TeleMoodleApplication.class, args);
	}

}
