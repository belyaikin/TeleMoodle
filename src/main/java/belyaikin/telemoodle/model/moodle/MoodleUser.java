package belyaikin.telemoodle.model.moodle;

import lombok.Data;

@Data
public class MoodleUser {
    private int userId;
    private String firstName;
    private String lastName;
}
