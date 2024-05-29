package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseManager {

    private static final String URL = "jdbc:clickhouse://s39gbj1fw4.eu-central-1.aws.clickhouse.cloud:8443/default";
    private static final String USER = "default";
    private static final String PASSWORD = "ZD7oxk.q5q_Qd";

    public Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", USER);
        properties.setProperty("password", PASSWORD);
        properties.setProperty("ssl", "true");
        properties.setProperty("sslcert", "/path/to/certificate.pem");
        return DriverManager.getConnection(URL, properties);
    }

    public void saveUser(String login, String password) {
        String query = "INSERT INTO Accounts (Login, Password) VALUES (?, ?)";
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, login);
            statement.setString(2, password);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void loginBot(String login, String password, long chatId){
        String query = "SELECT * FROM Accounts WHERE Login = ? and Password = ?";
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, login);
            statement.setString(2,password);
            ResultSet resultSet = statement.executeQuery();

            Bot.isAuthorized = resultSet.next();
            if(Bot.isAuthorized == true) {
                sendMessage(chatId, "Вы успешно вошли в аккаунт " + login);
            } else {
                sendMessage(chatId, "Ошибка авторизации");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void addFilmDB(String film_name, String description, int rating, String imgurl, String login){
        String query = "INSERT INTO filmslist (film_name, description, rating, imgurl, account_login) values (?,?,?,?,?)";
        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, film_name);
            statement.setString(2, description);
            statement.setInt(3, rating);
            statement.setString(4, imgurl);
            statement.setString(5, login);

            statement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public List<String> getFilmsDataDB(String login){
        List<String > filmsData = new ArrayList<>();
        String query = "SELECT film_name, rating FROM filmslist WHERE account_login = ? ORDER BY film_name";

        try (Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,login);
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                String film_name = resultSet.getString("film_name");
                int rating = resultSet.getInt("rating");

                String filmData = film_name + " - " + rating;
                filmsData.add(filmData);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return filmsData;
    }
    public int getFilmsCount(String login) {
        String query = "SELECT COUNT(*) FROM filmslist WHERE account_login = ?";
        int count = 0;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    public String[] getFilmByNameDB(String name, String login) {
        String query = "SELECT film_name, description, rating , imgurl FROM filmslist WHERE film_name = ? AND account_login = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, login);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new String[] {
                        resultSet.getString("film_name"),
                        resultSet.getString("description"),
                        resultSet.getString("rating"),
                        resultSet.getString("imgurl")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean checkFilm(String name, String login) {
        String query = "Select film_name FROM filmslist WHERE film_name = ? AND account_login = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)){
                statement.setString(1, name);
                statement.setString(2, login);
                ResultSet resultSet = statement.executeQuery();
                return resultSet.next();
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
    public void changeFilm(String point, String text, String selectedFilm, String login){
        String query = "ALTER TABLE filmslist UPDATE `" + point + "` = ? WHERE film_name = ? AND account_login = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)){
            if(point.equals("film_name") || point.equals("description") || point.equals("imgurl")){
                statement.setString(1, text);
                statement.setString(2, selectedFilm);
                statement.setString(3, login);
            } else {
                statement.setInt(1, Integer.parseInt(text));
                statement.setString(2, selectedFilm);
                statement.setString(3, login);
            }
            statement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.enableMarkdown(true);
        try {
            new Bot(this).execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}