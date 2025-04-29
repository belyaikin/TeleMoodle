package belyaikin.telemoodle.bot;

public enum CallbackType {
    SHOW_ALL_COURSES("all_courses");

    public final String callbackData;

    CallbackType(String callbackData) {
        this.callbackData = callbackData;
    }
}
