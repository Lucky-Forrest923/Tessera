package com.gameplatform.games.chess;

import com.gameplatform.model.BaseGame;
import com.gameplatform.model.Position;
import com.gameplatform.model.TurnBasedGame;
import com.gameplatform.renderer.CellClickContext;
import com.gameplatform.renderer.GamePanelRenderer;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Optional;

public class ChessRenderer implements GamePanelRenderer {
    private Position selectedPosition = null;

    @Override
    public StackPane renderCell(BaseGame game, int row, int col, boolean isHint, CellClickContext ctx) {
        StackPane cell = new StackPane();
        cell.setPrefSize(56, 56);
        cell.setMinSize(56, 56);
        cell.setAlignment(Pos.CENTER);

        boolean isLight = (row + col) % 2 == 0;
        String ch = game.getCellChar(row, col).trim();
        boolean isSelected = selectedPosition != null && selectedPosition.row() == row && selectedPosition.col() == col;
        String bgColor;

        if (isSelected) {
            bgColor = "#7FC97F";
        } else if (isLight) {
            bgColor = "#F0D9B5";
        } else {
            bgColor = "#B58863";
        }

        String border = isSelected ? "#2ecc71" : (isLight ? "#d4b896" : "#a67b52");
        double borderWidth = isSelected ? 2.5 : 0.5;
        cell.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + border + "; -fx-border-width: " + borderWidth + ";");

        if (!".".equals(ch) && !ch.isEmpty()) {
            String unicode = mapChessToUnicode(ch);
            Text text = new Text(unicode);
            text.setFont(Font.font(32));
            text.setFill(Character.isUpperCase(ch.charAt(0)) ? Color.BLACK : Color.WHITE);
            cell.getChildren().add(text);
        }

        cell.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) handleCellClick(game, row, col, ctx);
        });

        String finalBg = bgColor;
        cell.setOnMouseEntered(e -> {
            if (!isSelected) cell.setStyle("-fx-background-color: #f5deb3; -fx-border-color: #2ecc71; -fx-border-width: 2;");
        });
        cell.setOnMouseExited(e -> {
            if (!isSelected) cell.setStyle("-fx-background-color: " + finalBg + "; -fx-border-color: " + border + "; -fx-border-width: " + borderWidth + ";");
        });

        return cell;
    }

    private void handleCellClick(BaseGame game, int row, int col, CellClickContext ctx) {
        String ch = game.getCellChar(row, col).trim();
        boolean isEmpty = ".".equals(ch) || ch.isEmpty();

        if (selectedPosition == null) {
            if (isEmpty) {
                game.setStatus("Select your own piece first", true);
                ctx.refreshStatus();
                return;
            }
            if (game instanceof TurnBasedGame tbg) {
                boolean isWhiteTurn = tbg.getCurrentPlayerIndex() == 0;
                boolean isWhitePiece = Character.isLowerCase(ch.charAt(0));
                if (isWhiteTurn != isWhitePiece) {
                    game.setStatus("Not your turn - select your own piece", true);
                    ctx.refreshStatus();
                    return;
                }
            }
            selectedPosition = new Position(row, col);
            game.setStatus("Selected piece at " + GamePanelRenderer.coordToCmd(row, col) + ". Click target to move.", false);
            ctx.refreshBoard();
            ctx.refreshStatus();
        } else {
            if (!isEmpty && game instanceof TurnBasedGame tbg2) {
                boolean isWhiteTurn = tbg2.getCurrentPlayerIndex() == 0;
                boolean isWhitePiece = Character.isLowerCase(ch.charAt(0));
                if (isWhiteTurn == isWhitePiece) {
                    selectedPosition = new Position(row, col);
                    game.setStatus("Selected piece at " + GamePanelRenderer.coordToCmd(row, col) + ". Click target to move.", false);
                    ctx.refreshBoard();
                    ctx.refreshStatus();
                    return;
                }
            }
            String from = GamePanelRenderer.coordToCmd(selectedPosition.row(), selectedPosition.col());
            String to = GamePanelRenderer.coordToCmd(row, col);

            String cmd;
            if (isPawnPromotion(game, selectedPosition.row(), selectedPosition.col(), row)) {
                char promo = showPromotionDialog();
                if (promo == 0) { // cancelled — keep selection
                    game.setStatus("Promotion cancelled. Choose a target or select a different piece.", false);
                    ctx.refreshStatus();
                    return;
                }
                cmd = "m " + from + " " + to + " " + promo;
            } else {
                cmd = "m " + from + " " + to;
            }

            game.processCommand(cmd);
            selectedPosition = null;
            ctx.refreshAll();
        }
    }

    private String mapChessToUnicode(String ch) {
        if (ch == null || ch.isEmpty()) return "";
        return switch (ch) {
            case "K" -> "\u265A"; case "Q" -> "\u265B"; case "R" -> "\u265C";
            case "B" -> "\u265D"; case "N" -> "\u265E"; case "P" -> "\u265F";
            case "k" -> "\u2654"; case "q" -> "\u2655"; case "r" -> "\u2656";
            case "b" -> "\u2657"; case "n" -> "\u2658"; case "p" -> "\u2659";
            default -> ch;
        };
    }

    @Override
    public void clearSelection() {
        selectedPosition = null;
    }

    @Override
    public String getRuleText() {
        return "Standard chess rules. Two-click: select piece then target. Includes castling, en passant, promotion";
    }

    private boolean isPawnPromotion(BaseGame game, int fromRow, int fromCol, int toRow) {
        String ch = game.getCellChar(fromRow, fromCol).trim();
        if ("P".equals(ch)) {
            // Black pawn (display 'P') promotes at last row
            return toRow == game.getSize() - 1;
        }
        if ("p".equals(ch)) {
            // White pawn (display 'p') promotes at first row
            return toRow == 0;
        }
        return false;
    }

    private char showPromotionDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Queen", "Queen", "Rook", "Bishop", "Knight");
        dialog.setTitle("Pawn Promotion");
        dialog.setHeaderText("Pawn has reached the last rank!");
        dialog.setContentText("Promote to:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return 0;
        return switch (result.get()) {
            case "Rook" -> 'r';
            case "Bishop" -> 'b';
            case "Knight" -> 'n';
            default -> 'q';
        };
    }
}