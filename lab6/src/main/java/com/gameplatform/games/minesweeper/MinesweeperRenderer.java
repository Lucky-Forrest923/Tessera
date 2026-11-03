package com.gameplatform.games.minesweeper;

import com.gameplatform.model.BaseGame;
import com.gameplatform.renderer.CellClickContext;
import com.gameplatform.renderer.GamePanelRenderer;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MinesweeperRenderer implements GamePanelRenderer {
    @Override
    public StackPane renderCell(BaseGame game, int row, int col, boolean isHint, CellClickContext ctx) {
        StackPane cell = new StackPane();
        cell.setPrefSize(56, 56);
        cell.setMinSize(56, 56);
        cell.setAlignment(Pos.CENTER);

        Minesweeper msGame = (Minesweeper) game;
        CellStatus status = msGame.getCellState(row, col);
        Text text = null;

        switch (status) {
            case COVERED -> {
                cell.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 0% 100%, #78909c, #546e7a); -fx-border-color: #37474f; -fx-border-width: 1.5; -fx-border-radius: 2;");
            }
            case FLAGGED -> {
                text = new Text("\u2691");
                text.setFont(Font.font(24));
                text.setFill(Color.WHITE);
                cell.setStyle("-fx-background-color: #e74c3c; -fx-border-color: #c0392b; -fx-border-width: 1;");
            }
            case MINE -> {
                text = new Text("\u2739");
                text.setFont(Font.font(22));
                text.setStyle("-fx-font-weight: bold;");
                text.setFill(Color.YELLOW);
                cell.setStyle("-fx-background-color: #c0392b; -fx-border-color: #922b21; -fx-border-width: 1;");
            }
            default -> {
                cell.setStyle("-fx-background-color: #eceff1; -fx-border-color: #cfd8dc; -fx-border-width: 0.5;");
                if (status != CellStatus.OPENED_0) {
                    int num = status.ordinal() - CellStatus.OPENED_0.ordinal();
                    text = new Text(String.valueOf(num));
                    text.setFont(Font.font(20));
                    text.setStyle("-fx-font-weight: bold;");
                    text.setFill(switch (num) {
                        case 1 -> Color.BLUE;
                        case 2 -> Color.GREEN;
                        case 3 -> Color.RED;
                        case 4 -> Color.DARKBLUE;
                        case 5 -> Color.DARKRED;
                        case 6 -> Color.TEAL;
                        case 7 -> Color.BLACK;
                        case 8 -> Color.GRAY;
                        default -> Color.BLACK;
                    });
                }
            }
        }

        if (text != null) cell.getChildren().add(text);

        cell.setOnMouseClicked(e -> {
            ctx.handleCellClick(row, col, e.getButton() == MouseButton.SECONDARY);
        });

        return cell;
    }

    @Override
    public String getRuleText() {
        return "Open safe cells, avoid bombs. Right-click/Flag button to flag mines";
    }
}