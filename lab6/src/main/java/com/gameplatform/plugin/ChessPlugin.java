package com.gameplatform.plugin;

import com.gameplatform.games.chess.ChessGame;
import com.gameplatform.model.BaseGame;

public class ChessPlugin implements GamePlugin {
    @Override
    public String getTypeId() {
        return "chess";
    }

    @Override
    public String getDisplayName() {
        return "国际象棋";
    }

    @Override
    public BaseGame createGame(int size) {
        return new ChessGame(8);
    }

    @Override
    public boolean isDefaultGame() {
        return true;
    }

}