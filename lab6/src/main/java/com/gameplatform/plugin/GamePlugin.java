package com.gameplatform.plugin;

import com.gameplatform.model.BaseGame;

public interface GamePlugin {
    String getTypeId();
    String getDisplayName();
    BaseGame createGame(int size);
    boolean isDefaultGame();

}

