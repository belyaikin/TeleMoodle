package belyaikin.telemoodle.model.moodle.course;

public record Deadline(String name, long timeEnd, boolean lastDay, Course course) {
}
