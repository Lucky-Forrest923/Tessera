package com.othello.ui;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.othello.model.*;
import java.io.IOException;

/**
 * 终端界面展示类。
 * 利用 Lanterna 库在控制台中绘制图形化界面，并处理原始键盘输入。
 */
public class TerminalUI implements AutoCloseable {
    private final Screen screen;
    private final TextGraphics graphics;

    private static final int CELL_WIDTH = 5, CELL_HEIGHT = 3;
    private static final int OFFSET_X = 6, OFFSET_Y = 2;

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
     * 绘制游戏设置界面，引导用户输入棋盘大小。
     * @param input 当前输入缓冲区的内容
     * @param message 错误或提示消息
     * @throws IOException 屏幕刷新异常
     */
    public void drawSetupScreen(String input, String message) throws IOException {
        screen.clear();
        graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
        graphics.setForegroundColor(TextColor.ANSI.CYAN);

        graphics.putString(10, 5, "=== 黑白棋游戏设置 ===");
        graphics.putString(10, 7, "请输入棋盘大小 (必须为 4-16 之间的偶数): " + input);

        if (message != null && !message.isEmpty()) {
            graphics.setForegroundColor(TextColor.ANSI.RED);
            graphics.putString(10, 9, "提示: " + message);
        }

        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        graphics.putString(10, 11, "说明: 输入数字后按 Enter 确认，输入 'quit' 退出。");

        screen.setCursorPosition(new TerminalPosition(10 + 38 + input.length(), 7));
        screen.refresh();
    }

    /**
     * 绘制游戏运行时的核心棋盘界面。
     * @param board 棋盘数据对象
     * @param currentPlayer 当前回合玩家名称
     * @param input 当前输入框内容
     * @param statusMsg 状态或错误消息
     * @param gameOver 是否游戏结束
     * @param isError 消息是否为错误类型（影响颜色显示）
     * @throws IOException 屏幕刷新异常
     */
    public void drawGame(Board board, String currentPlayer, String input, String statusMsg, boolean gameOver, boolean isError) throws IOException {
        screen.clear();
        int size = board.getSize();

        graphics.setForegroundColor(TextColor.ANSI.CYAN);
        for (int c = 0; c < size; c++)
            graphics.putString(OFFSET_X + (c * CELL_WIDTH) + 2, OFFSET_Y - 1, String.valueOf((char)('A' + c)));

        for (int r = 0; r < size; r++) {
            graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
            graphics.setForegroundColor(TextColor.ANSI.CYAN);
            graphics.putString(OFFSET_X - 3, OFFSET_Y + (r * CELL_HEIGHT) + 1, String.valueOf(r + 1));

            for (int c = 0; c < size; c++) {
                int sx = OFFSET_X + (c * CELL_WIDTH), sy = OFFSET_Y + (r * CELL_HEIGHT);
                for (int h = 0; h < CELL_HEIGHT; h++) {
                    for (int w = 0; w < CELL_WIDTH; w++) {
                        graphics.setBackgroundColor((r + c) % 2 == 0 ? TextColor.Factory.fromString("#333333") : TextColor.Factory.fromString("#0000AA"));
                        graphics.setCharacter(sx + w, sy + h, ' ');
                    }
                }

                Piece p = board.getPiece(r, c);
                if (p != Piece.EMPTY) {
                    graphics.setForegroundColor(p == Piece.BLACK ? TextColor.ANSI.BLACK : TextColor.ANSI.WHITE);
                    graphics.putString(sx + 2, sy + 1, p.getDisplay().trim());
                } else {
                    graphics.setForegroundColor(TextColor.Factory.fromString("#888888"));
                    graphics.putString(sx + 2, sy + 1, "·");
                }
            }
        }


        int footerY = OFFSET_Y + (size * CELL_HEIGHT) + 2;
        graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        graphics.putString(OFFSET_X, footerY, String.format("Black: %d   White: %d   Turn: %s",
                board.getCount(Piece.BLACK), board.getCount(Piece.WHITE), currentPlayer));

        graphics.setForegroundColor(TextColor.ANSI.CYAN);
        String prompt = "> Input Move: ";
        graphics.putString(OFFSET_X, footerY + 2, prompt + input);

        // 消息行着色判断
        if (statusMsg != null && !statusMsg.isEmpty()) {
            graphics.setForegroundColor(gameOver ? TextColor.ANSI.GREEN : (isError ? TextColor.ANSI.RED : TextColor.ANSI.WHITE));
            graphics.putString(OFFSET_X, footerY + 4, (gameOver ? "★ " : (isError ? "✘ ERROR: " : "ℹ ")) + statusMsg);
        }

        screen.setCursorPosition(new TerminalPosition(OFFSET_X + prompt.length() + input.length(), footerY + 2));
        screen.refresh();
    }

    /**
     * 阻塞式读取用户键盘按键。
     * @return 键盘按键对象
     * @throws IOException 如果读取失败
     */
    public com.googlecode.lanterna.input.KeyStroke readInput() throws IOException { return screen.readInput(); }

    /** 停止屏幕模式 */
    @Override public void close() throws IOException { if (screen != null) screen.stopScreen(); }
}