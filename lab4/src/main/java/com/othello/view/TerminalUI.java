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
import com.othello.model.Piece;
import com.othello.model.Position;
import com.othello.text.GameTexts;

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

    /**
     * 创建终端界面并初始化 Lanterna 画面。
     *
     * @throws IOException 当终端初始化失败时抛出
     */
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

    /**
     * 绘制整块终端界面。
     *
     * @param game 当前游戏
     * @param allGames 所有游戏列表
     * @param currentNum 当前游戏编号
     * @param input 当前输入缓冲区内容
     * @param message 最近一次提示信息
     * @param isError 最近一次信息是否为错误
     * @throws IOException 当刷新界面失败时抛出
     */
    public void draw(BaseGame game, List<BaseGame> allGames, int currentNum,
                     String input, String message, boolean isError) throws IOException {
        screen.clear();
        int cols = screen.getTerminalSize().getColumns();
        int rows = screen.getTerminalSize().getRows();

        int boxTop = 0;
        int boxHeight = Math.max(10, rows - 4);
        int leftW = 28;
        int midW = 55;
        int rightW = Math.max(20, cols - leftW - midW - 6); // 列表宽

        drawBox(0, boxTop, leftW, boxHeight, GameTexts.BOARD_BOX_TITLE);
        drawBox(leftW + 1, boxTop, midW, boxHeight, GameTexts.STATUS_BOX_TITLE);
        drawBox(leftW + midW + 2, boxTop, rightW, boxHeight, GameTexts.GAME_LIST_BOX_TITLE);

        drawBoardInBox(game, 2, boxTop + 2, boxHeight - 4);
        drawStatusInBox(game, leftW + 3, boxTop + 1, midW - 4, boxHeight - 2, currentNum, message, isError);
        drawGameListInBox(allGames, leftW + midW + 4, boxTop + 1, rightW - 4, boxHeight - 2, currentNum);

        int inputY = rows - 2;
        int shortcutY = rows - 1;

        if (inputY > boxTop + boxHeight - 1) {
            putString(1, inputY, GameTexts.INPUT_PROMPT, CYAN);
            putString(8, inputY, input + "_", YELLOW);

            putString(1, shortcutY, GameTexts.SHORTCUTS, YELLOW);
        }

        screen.refresh();
    }

    /**
     * 绘制一个边框盒子。
     *
     * @param x 左上角横坐标
     * @param y 左上角纵坐标
     * @param w 宽度
     * @param h 高度
     * @param title 标题文本
     */
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

    /**
     * 绘制棋盘区域。
     *
     * @param game 当前游戏
     * @param x 起始横坐标
     * @param y 起始纵坐标
     * @param maxHeight 可用最大高度
     */
    private void drawBoardInBox(BaseGame game, int x, int y, int maxHeight) {
        int size = game.getSize();
        StringBuilder colHeader = new StringBuilder("  ");
        for (int i = 0; i < size; i++) colHeader.append((char)('a' + i)).append(" ");
        putString(x, y, colHeader.toString(), CYAN);

        for (int r = 0; r < size; r++) {
            if (r + 1 >= maxHeight) break;
            int rowY = y + 1 + r;
            putString(x, rowY, (r + 1) + " ", CYAN);
            for (int c = 0; c < size; c++) {
                String cell = game.getCellChar(r, c);
                TextColor color = WHITE;
                // 为 reversi 保留合法位置提示
                if (GameTexts.isType(game.getType(), GameTexts.TYPE_REVERSI) && isHint(game, r, c)) {
                    cell = "+ ";
                    color = CYAN;
                }
                putString(x + 2 + c * 2, rowY, cell, color);
            }
        }
    }

    /**
     * 绘制状态信息区域。
     *
     * @param game 当前游戏
     * @param x 起始横坐标
     * @param y 起始纵坐标
     * @param w 可用宽度
     * @param h 可用高度
     * @param currentNum 当前游戏编号
     * @param message 最近一次消息
     * @param isError 最近一次消息是否为错误
     */
    private void drawStatusInBox(BaseGame game, int x, int y, int w, int h, int currentNum, String message, boolean isError) {
        int cy = y + 1;
        putString(x, cy++, GameTexts.currentGameLabel(currentNum, game.getType()), WHITE);
        cy++;
        boolean isReversi = GameTexts.isType(game.getType(), GameTexts.TYPE_REVERSI);
        boolean isMinesweeper = GameTexts.isType(game.getType(), GameTexts.TYPE_MINESWEEPER);

        if (isMinesweeper) {
            // 扫雷状态
            putString(x, cy++, game.getGameSpecificStatus(), CYAN);
            if (game.isOver()) {
                putString(x, cy++, GameTexts.GAME_OVER_BANNER, GREEN);
                String result = GameTexts.minesweeperResult(game.getCode());
                if (!result.isEmpty()) {
                    putString(x, cy++, result, GREEN);
                }
            } else {
                putString(x, cy++, GameTexts.MINESWEEPER_SAFE_HINT, GREEN);
            }
        } else {
            boolean isBlack = game.getTurn() == Piece.BLACK;
            boolean isWhite = game.getTurn() == Piece.WHITE;

            if (game.isOver()) {
                putString(x, cy++, GameTexts.GAME_OVER_BANNER, GREEN);
                String reason = GameTexts.gameOverReasonLabel(game.getCode());
                if (!reason.isEmpty()) {
                    putString(x, cy++, reason, GREEN);
                }
                if (isReversi) {
                    int black = game.getBlackScore();
                    int white = game.getWhiteScore();
                    putString(x, cy++, GameTexts.reversiFinalScore(black, white), WHITE);
                    putString(x, cy++, GameTexts.reversiResult(black, white), GREEN);
                }
            } else {
                drawHighlightedLine(x, cy++, "黑方 " + (isBlack ? "← 当前回合" : ""), isBlack, w);
                drawHighlightedLine(x, cy++, "白方 " + (isWhite ? "← 当前回合" : ""), isWhite, w);
            }

            if (isReversi && !game.isOver()) {
                putString(x, cy, "当前比分：黑 / 白 = " + game.getBlackScore() + " / " + game.getWhiteScore(), WHITE);
            }
        }

        cy++;
        putString(x, cy++, GameTexts.LAST_MESSAGE_LABEL, CYAN);
        if (!message.isEmpty()) {
            String[] lines = message.split("\n");
            for (int i = 0; i < lines.length && cy < y + h - 7; i++) {
                putString(x, cy++, "> " + lines[i], isError ? RED : GREEN);
            }
        }

        cy = y + h - 8;
        if (cy > y + 6) {
            putString(x, cy++, "命令：", CYAN);
            putString(x, cy++, "- 落子(move)：1a / a1", CYAN);
            putString(x, cy++, "- 切换(switch)：2 / switch 2 / s 2", CYAN);
            putString(x, cy++, "- 插旗(flag，仅扫雷)：f 1a / flag a1", CYAN);
            putString(x, cy++, "- 新建(add)：peace / reversi / minesweeper", CYAN);
            putString(x, cy++, "- 跳过(pass，仅 reversi)：pass    - 退出：quit", CYAN);
            putString(x, cy++, "- 扫雷说明：相邻为 0 的格子不会自动展开", CYAN);
        }
    }

    /**
     * 绘制高亮或普通文本行。
     *
     * @param x 起始横坐标
     * @param y 起始纵坐标
     * @param text 文本内容
     * @param highlight 是否高亮
     * @param width 高亮时使用的宽度
     */
    private void drawHighlightedLine(int x, int y, String text, boolean highlight, int width) {
        if (highlight) {
            tg.setBackgroundColor(BLUE_BG);
            tg.setForegroundColor(TextColor.ANSI.BLACK);
            String padded = String.format("%-" + width + "s", text);
            tg.putString(x, y, padded);
            tg.setBackgroundColor(TextColor.ANSI.DEFAULT);
        } else {
            putString(x, y, text, WHITE);
        }
    }

    /**
     * 绘制右侧游戏列表。
     *
     * @param games 游戏列表
     * @param x 起始横坐标
     * @param y 起始纵坐标
     * @param w 可用宽度
     * @param h 可用高度
     * @param currentNum 当前游戏编号
     */
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


    /**
     * 在指定坐标输出字符串。
     *
     * @param x 起始横坐标
     * @param y 起始纵坐标
     * @param s 文本内容
     * @param fg 前景色
     */
    private void putString(int x, int y, String s, TextColor fg) {
        int rows = screen.getTerminalSize().getRows();
        int cols = screen.getTerminalSize().getColumns();
        if (y < 0 || y >= rows || x >= cols) return;
        tg.setForegroundColor(fg);
        tg.setBackgroundColor(TextColor.ANSI.DEFAULT);
        tg.putString(x, y, s);
    }

    /**
     * 判断某个位置是否是黑白棋的提示落点。
     *
     * @param game 当前游戏
     * @param r 行坐标
     * @param c 列坐标
     * @return 如果是提示落点则返回 {@code true}
     */
    private boolean isHint(BaseGame game, int r, int c) {
        for (Position p : game.getLegalMoves()) {
            if (p.row() == r && p.col() == c) return true;
        }
        return false;
    }

    /**
     * 读取一个按键输入，并转换成内部编码。
     *
     * @return 字符编码、控制键编码或无效键编码
     * @throws IOException 当读取输入失败时抛出
     */
    public int readKey() throws IOException {
        KeyStroke stroke = screen.readInput();
        if (stroke == null) return -3;
        if (stroke.getKeyType() == KeyType.Enter) return -1;
        if (stroke.getKeyType() == KeyType.Backspace) return -2;
        if (stroke.getKeyType() == KeyType.Character) return stroke.getCharacter();
        return -3;
    }

    /**
     * 关闭终端界面。
     *
     * @throws IOException 当关闭屏幕失败时抛出
     */
    @Override
    public void close() throws IOException {
        screen.stopScreen();
    }
}