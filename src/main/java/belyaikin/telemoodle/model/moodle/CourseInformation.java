package belyaikin.telemoodle.model.moodle;

public record CourseInformation(
        int id,
        String name,
        long startDate,
        long endDate
) { }
