package com.gameplatform.renderer;

import com.gameplatform.model.BaseGame;
import javafx.scene.layout.StackPane;

public interface GamePanelRenderer {
    StackPane renderCell(BaseGame game, int row, int col, boolean isHint, CellClickContext ctx);

    default void clearSelection() {}
    String getRuleText();

    static String coordToCmd(int row, int col) {
        return (char) ('a' + col) + String.valueOf(row + 1);
    }
}
