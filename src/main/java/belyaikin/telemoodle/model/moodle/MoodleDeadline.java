package belyaikin.telemoodle.model.moodle;

import lombok.Data;

@Data
public final class MoodleDeadline {
    private String assignmentName;
    private long timeEnd;
    private String isLastDay;
    private MoodleCourse course;

    public void setIsLastDay(boolean islastday) {
        this.isLastDay = islastday ? "Yes" : "No";
    }

}
