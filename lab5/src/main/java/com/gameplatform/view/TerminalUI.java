package com.gameplatform.view;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.gameplatform.model.BaseGame;
import com.gameplatform.model.Position;
import com.gameplatform.model.TurnBasedGame;
import com.gameplatform.model.HintProvider;
import com.gameplatform.text.GameTexts;

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
        TerminalSize tSize = new TerminalSize(125, 35);
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        factory.setInitialTerminalSize(tSize);

        SwingTerminalFrame frame = factory.createSwingTerminal();
        frame.setTitle(GameTexts.APP_TITLE);
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        screen = new TerminalScreen(frame);
        screen.startScreen();
        tg = screen.newTextGraphics();
    }

    public void draw(BaseGame game, List<BaseGame> allGames, int currentNum,
                     String input, String message, boolean isError, List<String> addableTypes) throws IOException {
        screen.clear();
        int cols = screen.getTerminalSize().getColumns();
        int rows = screen.getTerminalSize().getRows();

        int boxTop = 0;
        int boxHeight = Math.max(10, rows - 4);
        int leftW = 28;
        int midW = 55;
        int rightW = Math.max(20, cols - leftW - midW - 6);

        drawBox(0, boxTop, leftW, boxHeight, GameTexts.BOARD_BOX_TITLE);
        drawBox(leftW + 1, boxTop, midW, boxHeight, GameTexts.STATUS_BOX_TITLE);
        drawBox(leftW + midW + 2, boxTop, rightW, boxHeight, GameTexts.GAME_LIST_BOX_TITLE);

        drawBoardInBox(game, boxTop + 2, boxHeight - 4);
        drawStatusInBox(game, leftW + 3, boxTop + 1, midW - 4, boxHeight - 2, currentNum, message, isError, addableTypes);
        drawGameListInBox(allGames, leftW + midW + 4, boxTop + 1, rightW - 4, boxHeight - 2, currentNum);

        int inputY = rows - 2;
        if (inputY > boxTop + boxHeight - 1) {
            putString(1, inputY, GameTexts.INPUT_PROMPT, CYAN);
            putString(8, inputY, input + "_", YELLOW);
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

    private void drawBoardInBox(BaseGame game, int y, int maxHeight) {
        int size = game.getSize();
        StringBuilder colHeader = new StringBuilder("  ");
        for (int i = 0; i < size; i++) colHeader.append((char) ('a' + i)).append(" ");
        putString(2, y, colHeader.toString(), CYAN);

        List<Position> legalMoves = game instanceof HintProvider hp && hp.showLegalMovesAsHints()
                ? hp.getLegalMoves()
                : List.of();

        for (int r = 0; r < size; r++) {
            if (r + 1 >= maxHeight) break;
            int rowY = y + 1 + r;
            putString(2, rowY, (r + 1) + " ", CYAN);
            for (int c = 0; c < size; c++) {
                boolean isHint = legalMoves.contains(new Position(r, c));
                String cell = isHint ? "+ " : game.getCellChar(r, c);
                TextColor color = isHint ? CYAN : WHITE;
                putString(2 + 2 + c * 2, rowY, cell, color);
            }
        }
    }

    private void drawStatusInBox(BaseGame game, int x, int y, int w, int h, int currentNum, String message, boolean isError, List<String> addableTypes) {
        int cy = y + 1;
        putString(x, cy++, "当前游戏：#" + currentNum + "（" + game.getDisplayName() + "）", WHITE);
        cy++;

        if (game.isOver()) {
            putString(x, cy++, GameTexts.GAME_OVER_BANNER, GREEN);
        } else if (game instanceof TurnBasedGame tbg) {
            int current = tbg.getCurrentPlayerIndex();
            for (int i = 0; i < tbg.getPlayerCount(); i++) {
                boolean isActive = (i == current);
                String name = tbg.getPlayerName(i);
                if (isActive) {
                    tg.setBackgroundColor(BLUE_BG);
                    tg.setForegroundColor(TextColor.ANSI.BLACK);
                    tg.putString(x, cy++, String.format("%-" + w + "s", name + " ← 当前回合"));
                    tg.setBackgroundColor(TextColor.ANSI.DEFAULT);
                } else {
                    putString(x, cy++, name, WHITE);
                }
            }
        }

        for (String line : game.getExtraStatusLines()) {
            putString(x, cy++, line, WHITE);
        }

        cy++;
        putString(x, cy++, "上一次消息：", CYAN);
        if (!message.isEmpty()) {
            String[] lines = message.split("\n");
            for (int i = 0; i < lines.length && cy < y + h - 7; i++) {
                putString(x, cy++, "> " + lines[i], isError ? RED : GREEN);
            }
        }

        cy = y + h - 8;
        if (cy > y + 6) {
            putString(x, cy++, "命令：", CYAN);
            for (String cmdHint : game.getAvailableCommands()) {
                putString(x, cy++, cmdHint, CYAN);
            }
            putString(x, cy++, "- 切换(switch)：2 / switch 2 / s 2", CYAN);
            String addList = (addableTypes == null || addableTypes.isEmpty()) ? "未知" : String.join(" / ", addableTypes);
            putString(x, cy++, "- 新建(add)：" + addList, CYAN);
            putString(x, cy++, "- 演示(demo)：demo", CYAN);
            putString(x, cy++, "- 退出：quit", CYAN);
        }
    }

    private void drawGameListInBox(List<BaseGame> games, int x, int y, int w, int h, int currentNum) {
        int cy = y + 1;
        for (int i = 0; i < games.size(); i++) {
            if (cy >= y + h) break;
            boolean isCurrent = (i + 1) == currentNum;
            String status = games.get(i).isOver() ? "over" : "running";
            String text = (isCurrent ? "* " : "  ") + (i + 1) + ". " + games.get(i).getType() + " (" + status + ")";
            if (w > 0 && text.length() > w) {
                text = text.substring(0, Math.max(0, w - 1)) + "…";
            }
            putString(x, cy++, text, isCurrent ? YELLOW : WHITE);
        }
    }

    private void putString(int x, int y, String s, TextColor fg) {
        int rows = screen.getTerminalSize().getRows();
        int cols = screen.getTerminalSize().getColumns();
        if (y < 0 || y >= rows || x >= cols) return;
        tg.setForegroundColor(fg);
        tg.setBackgroundColor(TextColor.ANSI.DEFAULT);
        tg.putString(x, y, s);
    }

    public int pollKey() throws IOException {
        KeyStroke stroke = screen.pollInput();
        if (stroke == null) return -3;
        if (stroke.getKeyType() == KeyType.Enter) return -1;
        if (stroke.getKeyType() == KeyType.Backspace) return -2;
        if (stroke.getKeyType() == KeyType.Character) return stroke.getCharacter();
        return -3;
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