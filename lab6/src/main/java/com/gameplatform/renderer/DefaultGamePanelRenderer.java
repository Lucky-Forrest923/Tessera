package com.gameplatform.renderer;

import com.gameplatform.model.BaseGame;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class DefaultGamePanelRenderer implements GamePanelRenderer {
    @Override
    public StackPane renderCell(BaseGame game, int row, int col, boolean isHint, CellClickContext ctx) {
        StackPane cell = new StackPane();
        cell.setPrefSize(56, 56);
        cell.setMinSize(56, 56);
        cell.setAlignment(Pos.CENTER);
        boolean isLight = (row + col) % 2 == 0;
        cell.setStyle("-fx-background-color: " + (isLight ? "#ecf0f1" : "#bdc3c7")
                + "; -fx-border-color: #95a5a6; -fx-border-width: 0.5;");
        String ch = game.getCellChar(row, col).trim();
        if (!ch.isEmpty()) {
            Text t = new Text(ch);
            t.setFont(Font.font(18));
            cell.getChildren().add(t);
        }
        cell.setOnMouseClicked(e -> {
            try {
                game.processCommand(GamePanelRenderer.coordToCmd(row, col));
            } catch (Exception ex) {
                game.setStatus("Invalid input: " + ex.getMessage(), true);
            }
            ctx.refreshAll();
        });
        return cell;
    }

    @Override
    public String getRuleText() { return ""; }
}
