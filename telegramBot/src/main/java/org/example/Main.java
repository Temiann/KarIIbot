package org.example;

import org.example.bot.Bot;
import org.example.database.DatabaseManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        DatabaseManager databaseManager = new DatabaseManager();
        Bot testBot = new Bot(databaseManager);

        botsApi.registerBot(testBot);
    }
}