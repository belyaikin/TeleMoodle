package belyaikin.telemoodle.bot;

import belyaikin.telemoodle.bot.callback.CallbackRespondent;
import belyaikin.telemoodle.bot.callback.CallbackType;
import belyaikin.telemoodle.bot.callback.callbacks.CourseIdCallbackRespondent;
import belyaikin.telemoodle.bot.lang.Messages;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Component
public class CallbacksHandler {
    private final Map<CallbackType, CallbackRespondent> callbacks;

    public CallbacksHandler(
            @Autowired CourseIdCallbackRespondent courseIdCallbackRespondent
    ) {
        this.callbacks = Map.of(
                CallbackType.COURSE_ID, courseIdCallbackRespondent
        );
    }

    public SendMessage handleCallbacks(Update update) {
        JSONObject data = new JSONObject(update.getCallbackQuery().getData());
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (data.isEmpty())
            return new SendMessage(String.valueOf(chatId), Messages.NOT_IMPLEMENTED);

        // TODO: this doesn't work, fix!!!!!!!
        return callbacks.get(data.get("type")).execute("value", update);
    }
}
