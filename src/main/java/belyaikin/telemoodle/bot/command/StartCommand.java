package belyaikin.telemoodle.bot.command;

import belyaikin.telemoodle.bot.lang.Messages;
import belyaikin.telemoodle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class StartCommand implements Command {
    @Autowired private UserService userService;

    @Override
    public SendMessage execute(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());

        if (!userService.isUserRegistered(update.getMessage().getFrom().getId())) {
            sendMessage.setText(Messages.FIRST_TIME);
        }
        else {
            sendMessage.setText(Messages.CLICK_MENU_TO_VIEW_COMMANDS);
        }

        return sendMessage;
    }
}
