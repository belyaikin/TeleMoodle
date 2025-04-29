package belyaikin.telemoodle.model.moodle;

import lombok.Data;

@Data
public final class MoodleGrade {
    private int id;
    private String name;
    private long raw;
}