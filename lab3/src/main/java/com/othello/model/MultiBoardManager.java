package com.othello.model;

import java.util.ArrayList;
import java.util.List;

public class MultiBoardManager {
    private final List<BaseGame> games = new ArrayList<>();
    private int currentIndex = 0;

    public MultiBoardManager(int size) {
        games.add(new PeaceGame(size));
        games.add(new ReversiGame(size));
    }

    public void add(String type, int size) {
        if ("peace".equalsIgnoreCase(type)) {
            games.add(new PeaceGame(size));
        } else if ("reversi".equalsIgnoreCase(type)) {
            games.add(new ReversiGame(size));
        }
    }

    public BaseGame getActive() {
        return games.get(currentIndex);
    }

    public void select(int number) {
        if (number > 0 && number <= games.size()) {
            currentIndex = number - 1;
        }
    }

    public List<BaseGame> getGames() {
        return List.copyOf(games);
    }

    public int getCurrentBoardNumber() {
        return currentIndex + 1;
    }
}