package com.gameplatform.model;

public interface TurnBasedGame {
    int getPlayerCount();
    int getCurrentPlayerIndex();
    String getPlayerName(int index);
}

