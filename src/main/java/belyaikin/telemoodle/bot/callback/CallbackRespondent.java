package belyaikin.telemoodle.bot.callback;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface CallbackRespondent {
    SendMessage execute(String data, Update update);
}
