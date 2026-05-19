package com.othello.view;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.othello.io.GameIO;
import com.othello.io.GameViewData;
import com.othello.io.InputKey;
import com.othello.io.InputKeyType;
import com.othello.model.Board;
import com.othello.model.Piece;

import java.io.IOException;

/**
 * 终端界面展示类。
 * 负责绘制设置页与对局页，以及读取按键输入。
 */
public class TerminalUI implements GameIO {
    private static final int CELL_WIDTH = 5;
    private static final int CELL_HEIGHT = 3;
    private static final int OFFSET_X = 6;
    private static final int OFFSET_Y = 2;

    private final Screen screen;
    private final TextGraphics graphics;

    /**
     * 构造函数：初始化 Lanterna 屏幕并启动私有模式。
     * @throws IOException 如果终端创建失败
     */
    public TerminalUI() throws IOException {
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        factory.setInitialTerminalSize(new TerminalSize(100, 45));
        this.screen = new TerminalScreen(factory.createTerminal());
        this.screen.startScreen();
        this.graphics = screen.newTextGraphics();
    }

    /**
     * 绘制设置界面。
     * @param input 当前输入
     * @param message 提示信息
     * @throws IOException 刷新异常
     */
    @Override
    public void drawSetupScreen(String input, String message) throws IOException {
        screen.clear();
        graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
        graphics.setForegroundColor(TextColor.ANSI.CYAN);

        String setupPrompt = "请输入棋盘大小 (必须为 4-16 之间的偶数): ";
        graphics.putString(10, 5, "=== 黑白棋游戏设置 ===");
        graphics.putString(10, 7, setupPrompt + input);

        if (message != null && !message.isEmpty()) {
            graphics.setForegroundColor(TextColor.ANSI.RED);
            graphics.putString(10, 9, "提示: " + message);
        }

        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        graphics.putString(10, 11, "说明: 输入数字后按回车确认，输入退出结束。");

        placeCursorAtInput(10, 7, setupPrompt, input);
        screen.refresh();
    }

    /**
     * 绘制对局界面。
     * @param viewData 控制器提供的渲染数据
     * @throws IOException 刷新异常
     */
    @Override
    public void drawGame(GameViewData viewData) throws IOException {
        screen.clear();
        Board board = viewData.board();
        int size = board.getSize();

        graphics.setForegroundColor(TextColor.ANSI.CYAN);
        for (int c = 0; c < size; c++) {
            graphics.putString(OFFSET_X + (c * CELL_WIDTH) + 2, OFFSET_Y - 1, String.valueOf((char) ('A' + c)));
        }

        for (int r = 0; r < size; r++) {
            graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
            graphics.setForegroundColor(TextColor.ANSI.CYAN);
            graphics.putString(OFFSET_X - 3, OFFSET_Y + (r * CELL_HEIGHT) + 1, String.valueOf(r + 1));

            for (int c = 0; c < size; c++) {
                int sx = OFFSET_X + (c * CELL_WIDTH);
                int sy = OFFSET_Y + (r * CELL_HEIGHT);

                for (int h = 0; h < CELL_HEIGHT; h++) {
                    for (int w = 0; w < CELL_WIDTH; w++) {
                        graphics.setBackgroundColor((r + c) % 2 == 0
                                ? TextColor.Factory.fromString("#333333")
                                : TextColor.Factory.fromString("#0000AA"));
                        graphics.setCharacter(sx + w, sy + h, ' ');
                    }
                }

                Piece piece = board.getPiece(r, c);
                if (piece != Piece.EMPTY) {
                    graphics.setForegroundColor(piece == Piece.BLACK ? TextColor.ANSI.BLACK : TextColor.ANSI.WHITE);
                    graphics.putString(sx + 2, sy + 1, piece.getDisplay().trim());
                } else {
                    graphics.setForegroundColor(TextColor.Factory.fromString("#888888"));
                    graphics.putString(sx + 2, sy + 1, ".");
                }
            }
        }

        int footerY = OFFSET_Y + (size * CELL_HEIGHT) + 2;
        graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        graphics.putString(OFFSET_X, footerY, String.format(
                "黑棋: %d  白棋: %d | 当前棋盘: %d | 当前玩家: %s",
                viewData.blackCount(),
                viewData.whiteCount(),
                viewData.currentBoardNumber(),
                viewData.currentPlayer()));

        graphics.setForegroundColor(TextColor.ANSI.CYAN);
        String prompt = String.format("输入落子坐标(如 D4)、棋盘编号(1-%d)，或输入退出: ", viewData.totalBoards());
        graphics.putString(OFFSET_X, footerY + 2, prompt + viewData.input());

        if (viewData.statusMessage() != null && !viewData.statusMessage().isEmpty()) {
            graphics.setForegroundColor(viewData.gameOver() ? TextColor.ANSI.GREEN : (viewData.error() ? TextColor.ANSI.RED : TextColor.ANSI.WHITE));
            graphics.putString(OFFSET_X, footerY + 4,
                    (viewData.gameOver() ? "* " : (viewData.error() ? "错误: " : "提示: ")) + viewData.statusMessage());
        }

        placeCursorAtInput(OFFSET_X, footerY + 2, prompt, viewData.input());
        screen.refresh();
    }

    /**
     * 根据文本可视宽度定位光标，并保证坐标不越界。
     */
    private void placeCursorAtInput(int baseX, int y, String prefix, String input) {
        int textWidth = TerminalTextUtils.getColumnWidth(prefix)
                + TerminalTextUtils.getColumnWidth(input == null ? "" : input);

        TerminalSize size = screen.getTerminalSize();
        int maxX = Math.max(0, size.getColumns() - 1);
        int maxY = Math.max(0, size.getRows() - 1);

        int cursorX = Math.max(0, Math.min(baseX + textWidth, maxX));
        int cursorY = Math.max(0, Math.min(y, maxY));
        screen.setCursorPosition(new TerminalPosition(cursorX, cursorY));
    }

    /**
     * 阻塞读取一个按键。
     * @return 抽象按键对象
     * @throws IOException 读取异常
     */
    @Override
    public InputKey readInput() throws IOException {
        KeyStroke stroke = screen.readInput();
        if (stroke == null) {
            return new InputKey(InputKeyType.OTHER, null);
        }

        KeyType keyType = stroke.getKeyType();
        if (keyType == KeyType.Enter) {
            return new InputKey(InputKeyType.ENTER, null);
        }
        if (keyType == KeyType.Backspace) {
            return new InputKey(InputKeyType.BACKSPACE, null);
        }
        if (keyType == KeyType.Character) {
            return new InputKey(InputKeyType.CHARACTER, stroke.getCharacter());
        }
        return new InputKey(InputKeyType.OTHER, null);
    }

    /**
     * 关闭屏幕模式。
     * @throws IOException 关闭异常
     */
    @Override
    public void close() throws IOException {
        if (screen != null) {
            screen.stopScreen();
        }
    }
}

