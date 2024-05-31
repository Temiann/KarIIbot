package org.example.bot;

import org.example.utils.CommandList;
import org.example.database.DatabaseManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private DatabaseManager databaseManager;

    public Bot(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    private boolean isRegistration = false;
    private boolean isAddFilmCommand = false;
    public static boolean isAuthorized = false;
    private static boolean isLoginCommand = false;
    private boolean isSearchFilmCommand = false;
    private static String selectedFilm = "";
    private  boolean isSelectChangeFilm = false;
    private boolean isСhangedFilm = false;
    private static boolean backFlag = false;
    private String login = "";
    private String password = "";

    @Override
    public void onUpdateReceived(Update update){
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (isRegistration) {
                String[] credentials = messageText.split(" ");
                if (credentials.length == 2) {
                    login = credentials[0];
                    password = credentials[1];
                    databaseManager.saveUser(login, password);
                    String answer = "Вы успешно зарегистрировались!";
                    sendMessage(chatId, answer);
                    isRegistration = false;
                } else {
                    String answer = "Введите логин и пароль через пробел";
                    sendMessage(chatId, answer);
                }

            } else if(isLoginCommand && isAuthorized!=true){
                String[] credentials = messageText.split(" ");
                if (credentials.length == 2) {
                    login =  credentials[0];
                    password = credentials[1];
                    databaseManager.loginBot(login, password, chatId);
                    isLoginCommand = false;
                } else {
                    String answer = "Ошибка авторизации\n" +
                            "Введите логин и пароль через пробел";
                    sendMessage(chatId, answer);
                }

            } else if(isAuthorized == true){
                switch (messageText) {
                    case "/addFilm":
                        addFilmCommand(chatId);
                        break;
                    case "/changeFilm":
                        changeFilmCommand(chatId);
                        break;
                    case "/showListFilms": {
                        List<String> filmsData = databaseManager.getFilmsDataDB(login);
                        showListFilmsCommand(chatId, filmsData);
                    }
                    break;
                    case "/searchFilm":
                        searchFilmCommand(chatId);
                        break;
                    case "/logout":
                        logoutCommand(chatId);
                        break;
                    case "/commands":
                        commandsCommand(chatId);
                        break;
                    case "/faq":
                        commandsFAQ(chatId);
                        break;
                    default: {
                        if (isAddFilmCommand == true) {
                            userAddFilmData(chatId, messageText);
                            isAddFilmCommand = false;
                        } else if (isSearchFilmCommand == true){
                            try {
                                searchFilm(chatId, messageText);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                            isSearchFilmCommand = false;
                            break;
                        } else if (isСhangedFilm == true){
                            userChangeFilmData(chatId,messageText);
                            isСhangedFilm = false;
                            isSelectChangeFilm = false;
                        } else if (isSelectChangeFilm == true){
                            userChangeFilmSelectPoint(chatId, messageText);
                        } else {
                            sendMessage(chatId, "Неверная команда!");
                        }
                    }
                }
            } else {
                switch (messageText){
                    case "/start":
                        startCommand(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    case "/registration":
                        registrationCommand(chatId);
                        break;
                    case "/login":
                        loginCommand(chatId);
                        break;
                    case "/commands":
                        commandsCommand(chatId);
                        break;
                    case "/faq":
                        commandsFAQ(chatId);
                        break;
                    default: {
                        sendMessage(chatId, "Неверная команда!");
                    }
                }
            }
        }
    }

    private void startCommand(long chatId, String name){
        String answer = "Привет! " + name + ", для вызова списка команд данного бота введи /commands";
        sendMessage(chatId, answer);
    }
    private void registrationCommand(long chatId) {
        isRegistration = true;
        String answer = "Введите логин и пароль через пробел";
        sendMessage(chatId, answer);
    }
    private void loginCommand(long chatId){
        if(isAuthorized!=true){
            isLoginCommand = true;
            String answer = "Для авторизации введите логин и пароль через пробел";
            sendMessage(chatId, answer);
        } else {
            String answer = "Вы уже вошли в аккаунт, чтобы воспользоваться данной командой, выйдите из аккаунта";
            sendMessage(chatId, answer);
        }
    }
    private void commandsCommand(long chatId){
        CommandList commandList = new CommandList();
        String answer = "Вот список доступных команд: \n" + commandList.getCommands();
        sendMessage(chatId, answer);
    }
    private void commandsFAQ(long chatId){
        String answer = "         *Часто задаваемые вопросы* \n\n\n" +
                "- Для чего нужен этот бот? \n\n" +
                "Данный бот создан для ведения списка просмотренных фильмов, сериалов и аниме. " +
                "Бот используется только в учебных целях и не преследует коммерческих целей.\n\n" +
                "- Как начать пользоваться данным ботом? \n\n" +
                "Чтобы начать пользоваться данным ботом, вы должны зарегистрироваться. " +
                "Для этого введите команду /registration и следуйте инструкциям. " +
                "Если же у вас уже есть учетная запись, тогда просто войдите в аккаунт используя команду /login\n\n\n" +
                "Автор бота: _Temiann_ \n" +
                "Связаться со мной: _@temiannO_";
        sendMessage(chatId, answer);
    }
    //==========================================================================
    /*Account is auth*/
    private void addFilmCommand(long chatId){
        isAddFilmCommand = true;
        String answer = "Чтобы добавить фильм, введите данные в следующем порядке,\n добавляя один | через каждый пункт, не добавляя при этом пробелы \n" +
                "\n\n(название|описание фильма|оценка от 1 до 10|ссылка на картинку (если не нужна, то просто напишите '-'";
        sendMessage(chatId, answer);
    }
    private void changeFilmCommand(long chatId){
        String answer = "Введите название фильма, который хотите изменить";
        sendMessage(chatId, answer);
        isSelectChangeFilm = true;
    }
    private void userChangeFilmSelectPoint(long chatId, String messageText){
        System.out.println("Статус 2 = " + databaseManager.checkFilm(messageText, login));
        if(databaseManager.checkFilm(messageText, login) == true){
            isСhangedFilm = true;
            selectedFilm = messageText;
            String answer = "Выберите, что хотите изменить:\n" +
                    "1)Название фильма\n" +
                    "2)Описание фильма\n" +
                    "3)Рейтинг фильма\n" +
                    "4)Обложка фильма\n" +
                    "\n\n В ответе укажите пункт и через знак '|' изменения";
            sendMessage(chatId, answer);
        } else {
            isSelectChangeFilm = false;
            String answer = "Такого фильма нет в вашем списке!";
            sendMessage(chatId, answer);
        }
    }
    private void userChangeFilmData(long chatId, String messageText){
        String point = "";
        String[] credentials = messageText.split("\\|");
        if(credentials.length == 2){
            switch (credentials[0]){
                case "1":
                    point = "film_name";
                    break;
                case "2":
                    point = "description";
                    break;
                case "3":
                    point = "rating";
                    break;
                case "4":
                    point = "imgurl";
                    break;
            }
            databaseManager.changeFilm(point,credentials[1],selectedFilm,login);
            selectedFilm = "";
            String answer = "Успешно изменены данные";
            sendMessage(chatId, answer);
        } else {
            String answer = "Ошибка изменения данных";
            sendMessage(chatId, answer);
        }
    }
    private void userAddFilmData(long chatId, String messageText){
        String[] credentials = messageText.split("\\|");
        if (credentials.length == 4){
            String film_name = credentials[0];
            String description = credentials[1];
            String ratingStr = credentials[2];
            int rating = 0;
            try {
                rating = Integer.parseInt(ratingStr);
            } catch (NumberFormatException e){
                sendMessage(chatId, "Ошибка ввода рейтинга");
            }
            String imgurl = credentials[3];
            if(rating>=0 && rating<10){
                databaseManager.addFilmDB(film_name,description,rating,imgurl,login);
                sendMessage(chatId, "Вы успешно добавили фильм");
            } else {
                sendMessage(chatId, "Ошибка добавления фильма");
            }
        }
    }
    private void logoutCommand(long chatId){
        isAuthorized = false;
        String answer = "Вы вышли из аккаунта " + login;
        sendMessage(chatId, answer);
    }

    private void showListFilmsCommand(long chatId, List<String> filmsData){
        String answer = "Список фильмов:\n";
        for (String filmData : filmsData) {
            answer += filmData + "\n";
        }
        sendMessage(chatId,answer);
    }
    private void searchFilmCommand(long chatId){
        String answer = "Введите название фильма";
        sendMessage(chatId, answer);
        isSearchFilmCommand = true;
    }
    private void searchFilm(long chatId, String messageText) throws IOException, TelegramApiException {
        String[] film = databaseManager.getFilmByNameDB(messageText, login);
        if (film != null) {
            String answer = "Найдена ваша рецензия на фильм:\n\n" +
                    "Название: " + film[0] + "\n\n" +
                    "Описание: " + film[1] + "\n\n" +
                    "Оценка: " + film[2];
            sendMessage(chatId, answer);
            URL url = new URL(film[3]);
            InputStream stream = url.openStream();
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(String.valueOf(chatId));
            sendPhoto.setPhoto(new InputFile(stream, url.getFile()));
            execute(sendPhoto);
        } else {
            sendMessage(chatId, "Фильм с таким названием не найден.");
        }
    }
    //==========================================================================

    private void sendMessage(long chatId, String text) {
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
