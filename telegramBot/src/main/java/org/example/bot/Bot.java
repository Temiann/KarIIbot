package org.example.bot;

import org.example.database.DatabaseManager;
import org.example.handleCommand.Command;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

    private DatabaseManager databaseManager;
    private Command command;

    public Bot(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.command = new Command(this, databaseManager);
    }

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getChat().getFirstName();
            if (command.isRegistration()) {
                command.handleRegistration(chatId, messageText);
            } else if (command.isLoginCommand() && !command.isAuthorized()) {
                command.handleLogin(chatId, messageText);
            } else if (command.isAuthorized()) {
                command.handleAuthorizedCommands(chatId, messageText);
            } else {
                command.handleUnauthorizedCommands(chatId, messageText);
            }
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.enableMarkdown(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
