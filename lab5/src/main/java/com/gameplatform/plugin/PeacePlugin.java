package com.gameplatform.plugin;

import com.gameplatform.games.othello.PeaceGame;
import com.gameplatform.model.BaseGame;

public class PeacePlugin implements GamePlugin {
    @Override
    public String getTypeId() {
        return "peace";
    }

    @Override
    public String getDisplayName() {
        return "和平棋";
    }

    @Override
    public BaseGame createGame(int size) {
        return new PeaceGame(size);
    }

    @Override
    public boolean isDefaultGame() {
        return true;
    }
}