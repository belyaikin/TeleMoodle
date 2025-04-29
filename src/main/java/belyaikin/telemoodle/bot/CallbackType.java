package belyaikin.telemoodle.bot;

public enum CallbackType {
    SHOW_ALL_COURSES("all_courses");

    public final String string;

    CallbackType(String stringRepresentation) {
        this.string = stringRepresentation;
    }
}
