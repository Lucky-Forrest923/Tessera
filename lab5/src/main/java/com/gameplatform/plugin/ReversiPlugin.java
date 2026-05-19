package com.gameplatform.plugin;

import com.gameplatform.games.othello.ReversiGame;
import com.gameplatform.model.BaseGame;

public class ReversiPlugin implements GamePlugin {
    @Override
    public String getTypeId() {
        return "reversi";
    }

    @Override
    public String getDisplayName() {
        return "黑白棋";
    }

    @Override
    public BaseGame createGame(int size) {
        return new ReversiGame(size);
    }

    @Override
    public boolean isDefaultGame() {
        return true;
    }
}