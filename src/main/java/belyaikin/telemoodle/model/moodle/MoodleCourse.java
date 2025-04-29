package belyaikin.telemoodle.model.moodle;

import lombok.Data;

import java.util.List;

@Data
public final class MoodleCourse {
    private int id;
    private String name;
    private List<String> moduleNames;
}
