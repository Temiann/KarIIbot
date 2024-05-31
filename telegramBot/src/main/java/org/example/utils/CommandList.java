package org.example.utils;

import java.util.ArrayList;
import java.util.List;

public class CommandList {
    private List<String> commandList;

    public CommandList() {
        commandList = new ArrayList<>();
        addCommand("\n/registration - регистрация нового аккаунта \n");
        addCommand("/login - вход в аккаунт \n");
        addCommand("/logout - выход из аккаунта\n");
        addCommand("/addFilm - добавить фильм\n");
        addCommand("/changeFilm - изменить фильм\n");
        addCommand("/showListFilms - вывести список фильмов\n");
        addCommand("/searchFilm - поиск фильма по названию\n");
        addCommand("/commands - вывод всех доступных команд \n");
        addCommand("/faq - часто задаваемые вопросы\n");
    }

    public void addCommand(String command) {
        commandList.add(command);
    }

    public List<String> getCommandList() {
        return commandList;
    }

    public String getCommands(){
        StringBuilder commandBuilder = new StringBuilder();
        for (String command : commandList) {
            commandBuilder.append(command).append("\n");
        }
        return commandBuilder.toString();
    }
}
