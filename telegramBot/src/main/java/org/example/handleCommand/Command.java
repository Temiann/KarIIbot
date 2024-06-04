package org.example.handleCommand;

import org.example.bot.Bot;
import org.example.database.DatabaseManager;
import org.example.utils.CommandList;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class Command {

    private Bot bot;
    private DatabaseManager databaseManager;

    public boolean isRegistration = false;
    private boolean isAddFilmCommand = false;
    public static boolean isAuthorized = false;
    public static boolean isLoginCommand = false;
    private boolean isSearchFilmCommand = false;
    private static String selectedFilm = "";
    private boolean isSelectChangeFilm = false;
    private boolean isСhangedFilm = false;
    private static boolean backFlag = false;
    private String login = "";
    private String password = "";
    private String username = "";

    public Command(Bot bot, DatabaseManager databaseManager) {
        this.bot = bot;
        this.databaseManager = databaseManager;
    }

    //##################################
    //              Handles
    //#################################
    public void handleRegistration(long chatId, String messageText) {
        String[] credentials = messageText.split(" ");
        if (credentials.length == 2) {
            login = credentials[0];
            password = credentials[1];
            databaseManager.saveUser(login, password);
            String answer = "Вы успешно зарегистрировались!";
            bot.sendMessage(chatId, answer);
            isRegistration = false;
        } else {
            String answer = "Введите логин и пароль через пробел";
            bot.sendMessage(chatId, answer);
        }
    }

    public void handleLogin(long chatId, String messageText) {
        String[] credentials = messageText.split(" ");
        if (credentials.length == 2) {
            login = credentials[0];
            password = credentials[1];
            databaseManager.loginBot(login, password, chatId);
            isLoginCommand = false;
        } else {
            String answer = "Ошибка авторизации\nВведите логин и пароль через пробел";
            bot.sendMessage(chatId, answer);
        }
    }

    public void handleUnauthorizedCommands(long chatId, String messageText) {
        switch (messageText) {
            case "/start":
                startCommand(chatId, username);
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
            default:
                bot.sendMessage(chatId, "Неверная команда!");
        }
    }

    public void handleAuthorizedCommands(long chatId, String messageText) {
        switch (messageText) {
            case "/addFilm":
                addFilmCommand(chatId);
                break;
            case "/changeFilm":
                changeFilmCommand(chatId);
                break;
            case "/showListFilms":
                List<String> filmsData = databaseManager.getFilmsDataDB(login);
                showListFilmsCommand(chatId, filmsData);
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
            default:
                if (isAddFilmCommand) {
                    userAddFilmData(chatId, messageText);
                    isAddFilmCommand = false;
                } else if (isSearchFilmCommand) {
                    try {
                        searchFilm(chatId, messageText);
                    } catch (IOException | TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    isSearchFilmCommand = false;
                } else if (isСhangedFilm) {
                    userChangeFilmData(chatId, messageText);
                    isСhangedFilm = false;
                    isSelectChangeFilm = false;
                } else if (isSelectChangeFilm) {
                    userChangeFilmSelectPoint(chatId, messageText);
                } else {
                    bot.sendMessage(chatId, "Неверная команда!");
                }
        }
    }
    //##########################
    //commands
    //#########################
    private void startCommand(long chatId, String name) {
        String answer = "Привет! " + name + ", для вызова списка команд данного бота введи /commands";
        bot.sendMessage(chatId, answer);
    }

    private void registrationCommand(long chatId) {
        isRegistration = true;
        String answer = "Введите логин и пароль через пробел";
        bot.sendMessage(chatId, answer);
    }

    private void loginCommand(long chatId) {
        if (!isAuthorized) {
            isLoginCommand = true;
            String answer = "Для авторизации введите логин и пароль через пробел";
            bot.sendMessage(chatId, answer);
        } else {
            String answer = "Вы уже вошли в аккаунт, чтобы воспользоваться данной командой, выйдите из аккаунта";
            bot.sendMessage(chatId, answer);
        }
    }

    private void commandsCommand(long chatId) {
        CommandList commandList = new CommandList();
        String answer = "Вот список доступных команд: \n" + commandList.getCommands();
        bot.sendMessage(chatId, answer);
    }

    private void commandsFAQ(long chatId) {
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
        bot.sendMessage(chatId, answer);
    }
    //##############################################
    //               when auth = true
    //###############################################
    private void addFilmCommand(long chatId) {
        isAddFilmCommand = true;
        String answer = "Чтобы добавить фильм, введите данные в следующем порядке,\n добавляя один | через каждый пункт, не добавляя при этом пробелы \n" +
                "\n\n(название|описание фильма|оценка от 1 до 10|ссылка на картинку (если не нужна, то просто напишите '-'";
        bot.sendMessage(chatId, answer);
    }

    private void changeFilmCommand(long chatId) {
        String answer = "Введите название фильма, который хотите изменить";
        bot.sendMessage(chatId, answer);
        isSelectChangeFilm = true;
    }

    private void userChangeFilmSelectPoint(long chatId, String messageText) {
        if (databaseManager.checkFilm(messageText, login)) {
            isСhangedFilm = true;
            selectedFilm = messageText;
            String answer = "Выберите, что хотите изменить:\n" +
                    "1)Название фильма\n" +
                    "2)Описание фильма\n" +
                    "3)Рейтинг фильма\n" +
                    "4)Обложка фильма\n" +
                    "\n\n В ответе укажите пункт и через знак '|' изменения";
            bot.sendMessage(chatId, answer);
        } else {
            isSelectChangeFilm = false;
            String answer = "Такого фильма нет в вашем списке!";
            bot.sendMessage(chatId, answer);
        }
    }

    private void userChangeFilmData(long chatId, String messageText) {
        String point = "";
        String[] credentials = messageText.split("\\|");
        if (credentials.length == 2) {
            switch (credentials[0]) {
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
            databaseManager.changeFilm(point, credentials[1], selectedFilm, login);
            selectedFilm = "";
            String answer = "Успешно изменены данные";
            bot.sendMessage(chatId, answer);
        } else {
            String answer = "Ошибка изменения данных";
            bot.sendMessage(chatId, answer);
        }
    }

    private void userAddFilmData(long chatId, String messageText) {
        String[] credentials = messageText.split("\\|");
        if (credentials.length == 4) {
            String film_name = credentials[0];
            String description = credentials[1];
            String ratingStr = credentials[2];
            int rating = 0;
            try {
                rating = Integer.parseInt(ratingStr);
            } catch (NumberFormatException e) {
                bot.sendMessage(chatId, "Ошибка ввода рейтинга");
            }
            String imgurl = credentials[3];
            if (rating >= 0 && rating < 10) {
                databaseManager.addFilmDB(film_name, description, rating, imgurl, login);
                bot.sendMessage(chatId, "Вы успешно добавили фильм");
            } else {
                bot.sendMessage(chatId, "Ошибка добавления фильма");
            }
        }
    }

    private void logoutCommand(long chatId) {
        isAuthorized = false;
        String answer = "Вы вышли из аккаунта " + login;
        bot.sendMessage(chatId, answer);
    }

    private void showListFilmsCommand(long chatId, List<String> filmsData) {
        String answer = "Список фильмов:\n";
        for (String filmData : filmsData) {
            answer += filmData + "\n";
        }
        bot.sendMessage(chatId, answer);
    }

    private void searchFilmCommand(long chatId) {
        String answer = "Введите название фильма";
        bot.sendMessage(chatId, answer);
        isSearchFilmCommand = true;
    }

    private void searchFilm(long chatId, String messageText) throws IOException, TelegramApiException {
        String[] film = databaseManager.getFilmByNameDB(messageText, login);
        if (film != null) {
            String answer = "Найдена ваша рецензия на фильм:\n\n" +
                    "Название: " + film[0] + "\n\n" +
                    "Описание: " + film[1] + "\n\n" +
                    "Оценка: " + film[2];
            bot.sendMessage(chatId, answer);
            URL url = new URL(film[3]);
            InputStream stream = url.openStream();
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(String.valueOf(chatId));
            sendPhoto.setPhoto(new InputFile(stream, url.getFile()));
            bot.execute(sendPhoto);
        } else {
            bot.sendMessage(chatId, "Фильм с таким названием не найден.");
        }
    }
    //############################################################################################################
    public boolean isRegistration() {
        return isRegistration;
    }

    public boolean isLoginCommand() {
        return isLoginCommand;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }
}
