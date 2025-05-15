package belyaikin.telemoodle.model.moodle.course;

public record Grade(
        int id,
        String name,
        long raw,
        Course course
) { }
