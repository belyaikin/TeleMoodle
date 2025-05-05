package belyaikin.telemoodle.model.moodle;

import lombok.Data;

@Data
public final class MoodleDeadline {
    private String assignmentName;
    private long timeEnd;
    private boolean isLastDay;
    private MoodleCourse course;
}