package com.gameplatform.plugin;

import com.gameplatform.games.minesweeper.Minesweeper;
import com.gameplatform.model.BaseGame;

public class MinesweeperPlugin implements GamePlugin {
    @Override
    public String getTypeId() {
        return "minesweeper";
    }

    @Override
    public String getDisplayName() {
        return "扫雷";
    }

    @Override
    public BaseGame createGame(int size) {
        return new Minesweeper(size);
    }

    @Override
    public boolean isDefaultGame() {
        return true;
    }
}