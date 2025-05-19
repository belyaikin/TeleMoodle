package belyaikin.telemoodle.model.moodle.course;

public record Course(
        int id,
        String name,
        String teacher,
        long startDate,
        long endDate
) { }
