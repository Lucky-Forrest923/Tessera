package com.gameplatform.games.othello;

import com.gameplatform.model.BaseGame;
import com.gameplatform.renderer.CellClickContext;
import com.gameplatform.renderer.GamePanelRenderer;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class OthelloStyleRenderer implements GamePanelRenderer {
    private final String ruleText;

    public OthelloStyleRenderer(String ruleText) {
        this.ruleText = ruleText;
    }

    @Override
    public StackPane renderCell(BaseGame game, int row, int col, boolean isHint, CellClickContext ctx) {
        StackPane cell = new StackPane();
        cell.setPrefSize(56, 56);
        cell.setMinSize(56, 56);
        cell.setAlignment(Pos.CENTER);

        String pieceChar = game.getCellChar(row, col).trim();
        boolean isLight = (row + col) % 2 == 0;
        String bgColor = isLight ? "#2ecc71" : "#27ae60";
        Text text = null;

        if (isHint) {
            bgColor = "#f1c40f";
            text = new Text("+");
            text.setFont(Font.font(22));
            text.setStyle("-fx-font-weight: bold;");
            text.setFill(Color.web("#2c3e50"));
        } else if ("\u25CF".equals(pieceChar)) {
            text = new Text("\u25CF");
            text.setFont(Font.font(28));
            text.setFill(Color.BLACK);
        } else if ("\u25CB".equals(pieceChar)) {
            text = new Text("\u25CB");
            text.setFont(Font.font(28));
            text.setFill(Color.WHITE);
        }

        String finalBg = bgColor;
        cell.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: #1a5e2a; -fx-border-width: 0.5;");
        if (text != null) cell.getChildren().add(text);

        cell.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                ctx.handleCellClick(row, col, false);
            }
        });
        cell.setOnMouseEntered(e -> {
            if (!isHint) cell.setStyle("-fx-background-color: #3498db; -fx-border-color: #2c3e50; -fx-border-width: 1.5;");
        });
        cell.setOnMouseExited(e -> {
            cell.setStyle("-fx-background-color: " + finalBg + "; -fx-border-color: #1a5e2a; -fx-border-width: 0.5;");
        });

        return cell;
    }

    @Override
    public String getRuleText() { return ruleText; }
}