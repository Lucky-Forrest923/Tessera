package com.othello.view;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.othello.model.BaseGame;
import com.othello.model.GameStatusCode;
import com.othello.model.Piece;
import com.othello.model.Position;

import java.io.IOException;
import java.util.List;

public class TerminalUI implements AutoCloseable {
    private final Screen screen;
    private final TextGraphics tg;

    private static final TextColor CYAN = TextColor.ANSI.CYAN;
    private static final TextColor YELLOW = TextColor.ANSI.YELLOW_BRIGHT;
    private static final TextColor WHITE = TextColor.ANSI.WHITE;
    private static final TextColor RED = TextColor.ANSI.RED;
    private static final TextColor GREEN = TextColor.ANSI.GREEN;
    private static final TextColor BLUE_BG = TextColor.ANSI.CYAN;

    public TerminalUI() throws IOException {
        // --- 缩小窗口设置 ---
        // 设置终端字符行列数为 125x35
        TerminalSize tSize = new TerminalSize(125, 35);
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        factory.setInitialTerminalSize(tSize);

        SwingTerminalFrame frame = factory.createSwingTerminal();
        frame.setTitle("Othello Game Terminal");

        // 设置物理窗口大小为 1100x700 像素 (比之前小了很多)
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // 删除了 MAXIMIZED_BOTH 确保它以指定大小打开

        screen = new TerminalScreen(frame);
        screen.startScreen();
        tg = screen.newTextGraphics();
    }

    public void draw(BaseGame game, List<BaseGame> allGames, int currentNum,
                     String input, String message, boolean isError) throws IOException {
        screen.clear();
        int cols = screen.getTerminalSize().getColumns();
        int rows = screen.getTerminalSize().getRows();

        // 布局参数：根据缩小后的窗口比例分配宽度
        int boxTop = 0;
        int boxHeight = Math.max(10, rows - 4);
        int leftW = 28; // 棋盘宽
        int midW = 55;  // 状态宽
        int rightW = Math.max(20, cols - leftW - midW - 6); // 列表宽

        // 1. 绘制边框
        drawBox(0, boxTop, leftW, boxHeight, " BOARD ");
        drawBox(leftW + 1, boxTop, midW, boxHeight, " STATUS ");
        drawBox(leftW + midW + 2, boxTop, rightW, boxHeight, " GAME LIST ");

        // 2. 填充内容
        drawBoardInBox(game, 2, boxTop + 2, boxHeight - 4);
        drawStatusInBox(game, leftW + 3, boxTop + 1, midW - 4, boxHeight - 2, currentNum, message, isError);
        drawGameListInBox(allGames, leftW + midW + 4, boxTop + 1, rightW - 4, boxHeight - 2, currentNum);

        // 3. 底部交互区
        int inputY = rows - 2;
        int shortcutY = rows - 1;

        if (inputY > boxTop + boxHeight - 1) {
            putString(1, inputY, "Input> ", CYAN, null);
            putString(8, inputY, input + "_", YELLOW, null);

            String shortcuts = "Shortcuts: D4 (Move) | 1-9 (Switch) | peace/reversi (New) | quit";
            putString(1, shortcutY, shortcuts, YELLOW, null);
        }

        screen.refresh();
    }

    private void drawBox(int x, int y, int w, int h, String title) {
        if (w <= 0 || h <= 0) return;
        tg.setForegroundColor(WHITE);
        String horizontal = "+" + "-".repeat(w - 2) + "+";
        tg.putString(x, y, horizontal);
        tg.putString(x, y + h - 1, horizontal);
        for (int i = 1; i < h - 1; i++) {
            tg.putString(x, y + i, "|");
            tg.putString(x + w - 1, y + i, "|");
        }
        tg.setForegroundColor(CYAN);
        tg.putString(x + 2, y, title);
    }

    private void drawBoardInBox(BaseGame game, int x, int y, int maxHeight) {
        int size = game.getBoard().getSize();
        StringBuilder colHeader = new StringBuilder("  ");
        for (int i = 0; i < size; i++) colHeader.append((char)('a' + i)).append(" ");
        putString(x, y, colHeader.toString(), CYAN, null);

        for (int r = 0; r < size; r++) {
            if (r + 1 >= maxHeight) break;
            int rowY = y + 1 + r;
            putString(x, rowY, (r + 1) + " ", CYAN, null);
            for (int c = 0; c < size; c++) {
                Piece p = game.getBoard().getPiece(r, c);
                String sym = ". ";
                TextColor color = WHITE;
                if (p == Piece.BLACK) { sym = "● ";
                }
                else if (p == Piece.WHITE) { sym = "○ ";
                }
                else if ("reversi".equals(game.getType()) && isHint(game, r, c)) {
                    sym = "+ ";
                    color = CYAN;
                }
                putString(x + 2 + c * 2, rowY, sym, color, null);
            }
        }
    }

    private void drawStatusInBox(BaseGame game, int x, int y, int w, int h, int currentNum, String message, boolean isError) {
        int cy = y + 1;
        putString(x, cy++, "Current game: #" + currentNum, WHITE, null);
        cy++;
        boolean isReversi = "reversi".equals(game.getType());

        boolean isBlack = game.getTurn() == Piece.BLACK;
        boolean isWhite = game.getTurn() == Piece.WHITE;

        if (game.isOver()) {
            putString(x, cy++, "!!! GAME OVER !!!", GREEN, null);
            String reason = getGameOverReason(game.getCode());
            if (!reason.isEmpty()) {
                putString(x, cy++, "Reason: " + reason, GREEN, null);
            }
            if (isReversi) {
                int black = game.getBlackScore();
                int white = game.getWhiteScore();
                putString(x, cy++, "Final Score B/W: " + black + " / " + white, WHITE, null);
                String result = black > white
                        ? "Result: Player1 (Black) wins"
                        : (white > black ? "Result: Player2 (White) wins" : "Result: Draw");
                putString(x, cy++, result, GREEN, null);
            }
        } else {
            drawHighlightedLine(x, cy++, "Player1 (Black) " + (isBlack ? " <- turn" : ""), isBlack, w);
            drawHighlightedLine(x, cy++, "Player2 (White) " + (isWhite ? " <- turn" : ""), isWhite, w);
        }

        cy++;
        if (isReversi && !game.isOver()) {
            putString(x, cy++, "Score B/W: " + game.getBlackScore() + " / " + game.getWhiteScore(), WHITE, null);
        }

        cy++;
        putString(x, cy++, "Last message:", CYAN, null);
        if (!message.isEmpty()) {
            String[] lines = message.split("\n");
            for (int i = 0; i < lines.length && cy < y + h - 7; i++) {
                putString(x, cy++, "> " + lines[i], isError ? RED : GREEN, null);
            }
        }

        cy = y + h - 6;
        if (cy > y + 6) {
            putString(x, cy++, "Commands: d4 (move) | 1-9 (switch)", CYAN, null);
            putString(x, cy++, "          peace/reversi (new) | quit", CYAN, null);
        }
    }

    private void drawHighlightedLine(int x, int y, String text, boolean highlight, int width) {
        if (highlight) {
            tg.setBackgroundColor(BLUE_BG);
            tg.setForegroundColor(TextColor.ANSI.BLACK);
            String padded = String.format("%-" + width + "s", text);
            tg.putString(x, y, padded);
            tg.setBackgroundColor(TextColor.ANSI.DEFAULT);
        } else {
            putString(x, y, text, WHITE, null);
        }
    }

    private void drawGameListInBox(List<BaseGame> games, int x, int y, int w, int h, int currentNum) {
        int cy = y + 1;
        for (int i = 0; i < games.size(); i++) {
            if (cy >= y + h) break;
            boolean isCurrent = (i + 1) == currentNum;
            String text = (isCurrent ? "* " : "  ") + (i + 1) + ". " + games.get(i).getType();
            putString(x, cy++, text, isCurrent ? YELLOW : WHITE, null);
        }
    }

    private String getGameOverReason(GameStatusCode code) {
        if (code == GameStatusCode.GAME_OVER_FULL) {
            return "Board is full";
        }
        if (code == GameStatusCode.GAME_OVER_NO_MOVES) {
            return "Both players have no legal moves";
        }
        return "";
    }

    private void putString(int x, int y, String s, TextColor fg, TextColor bg) {
        int rows = screen.getTerminalSize().getRows();
        int cols = screen.getTerminalSize().getColumns();
        if (y < 0 || y >= rows || x >= cols) return;
        tg.setForegroundColor(fg);
        tg.setBackgroundColor(bg != null ? bg : TextColor.ANSI.DEFAULT);
        tg.putString(x, y, s);
    }

    private boolean isHint(BaseGame game, int r, int c) {
        for (Position p : game.getLegalMoves()) {
            if (p.row() == r && p.col() == c) return true;
        }
        return false;
    }

    public int readKey() throws IOException {
        KeyStroke stroke = screen.readInput();
        if (stroke == null) return -3;
        if (stroke.getKeyType() == KeyType.Enter) return -1;
        if (stroke.getKeyType() == KeyType.Backspace) return -2;
        if (stroke.getKeyType() == KeyType.Character) return stroke.getCharacter();
        return -3;
    }

    @Override
    public void close() throws IOException {
        screen.stopScreen();
    }
}