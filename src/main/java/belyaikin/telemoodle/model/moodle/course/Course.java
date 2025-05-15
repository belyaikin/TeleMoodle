package belyaikin.telemoodle.model.moodle.course;

public record Course(
        int id,
        String name,
        long startDate,
        long endDate
) { }
