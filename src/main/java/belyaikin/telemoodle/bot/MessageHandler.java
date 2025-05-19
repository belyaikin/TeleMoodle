package belyaikin.telemoodle.bot;

import belyaikin.telemoodle.bot.command.Command;
import belyaikin.telemoodle.bot.command.StartCommand;
import belyaikin.telemoodle.bot.lang.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Component
public class MessageHandler {
    private final Map<String, Command> commands;

    public MessageHandler(@Autowired StartCommand startCommand) {
        this.commands = Map.of("/start", startCommand);
    }

    public SendMessage handleCommand(Update update) {
        String text = update.getMessage().getText().split(" ")[0];
        long chatId = update.getMessage().getChatId();

        Command command = commands.get(text);

        if (command != null) {
            return command.execute(update);
        } else {
            return new SendMessage(String.valueOf(chatId), Messages.WRONG_COMMAND);
        }
    }

    public SendMessage handleText(Update update) {
        long chatId = update.getMessage().getChatId();

        // TODO: Registration and other non-command related stuff
        return new SendMessage(String.valueOf(chatId), Messages.NOT_IMPLEMENTED);
    }
}
