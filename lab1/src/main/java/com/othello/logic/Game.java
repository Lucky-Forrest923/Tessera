package com.othello.logic;

import com.googlecode.lanterna.input.*;
import com.othello.model.*;
import com.othello.ui.TerminalUI;

/**
 * 游戏核心控制器类。
 * 负责管理游戏循环，包括初始化设置、回合切换、输入处理及逻辑判定
 */
public class Game implements AutoCloseable {
    private Board board;
    private final TerminalUI ui;
    private Piece currentTurn = Piece.BLACK;
    private final StringBuilder inputBuffer = new StringBuilder();
    private boolean gameOver = false;
    private String statusMessage = "";
    private boolean isError = false;

    /**
     * 游戏构造函数。
     * 初始化终端 UI 界面。
     * @throws Exception 如果终端初始化失败
     */
    public Game() throws Exception {
        this.ui = new TerminalUI();
    }

    /**
     * 启动游戏主程序。
     * 负责设置棋盘大小和进入落子循环。
     * @throws Exception 处理输入或刷新屏幕时的 IO 异常
     */
    public void start() throws Exception {
        while (this.board == null) {
            ui.drawSetupScreen(inputBuffer.toString(), statusMessage);
            KeyStroke ks = ui.readInput();

            if (ks.getKeyType() == KeyType.Enter) {
                String cmd = inputBuffer.toString().trim();
                if (cmd.equalsIgnoreCase("quit")) return;

                handleSetup(cmd);
                inputBuffer.setLength(0);
            }
            else if (ks.getKeyType() == KeyType.Backspace && inputBuffer.length() > 0) {
                inputBuffer.deleteCharAt(inputBuffer.length() - 1);
            }
            else if (ks.getKeyType() == KeyType.Character) {
                inputBuffer.append(ks.getCharacter());
            }
        }

        statusMessage = "游戏开始！黑棋先行。";
        while (true) {
            checkGameOver();
            String currentPlayer = gameOver ? "游戏结束" : currentTurn.name();
            ui.drawGame(board, currentPlayer, inputBuffer.toString(), statusMessage, gameOver, isError);

            KeyStroke ks = ui.readInput();
            if (ks.getKeyType() == KeyType.Enter) {
                String cmd = inputBuffer.toString().trim();
                if (cmd.equalsIgnoreCase("quit")) break;

                if (!gameOver) {
                    handleMove(cmd);
                } else {
                    statusMessage = "游戏已结束。请输入 'quit' 退出。";
                    isError = false;
                }
                inputBuffer.setLength(0);
            }
            else if (ks.getKeyType() == KeyType.Backspace && inputBuffer.length() > 0) {
                inputBuffer.deleteCharAt(inputBuffer.length() - 1);
            }
            else if (ks.getKeyType() == KeyType.Character && inputBuffer.length() < 10) {
                inputBuffer.append(ks.getCharacter());
            }
        }
    }

    /**
     * 处理初始化时的棋盘大小输入。
     * @param cmd 用户输入的字符串
     */
    private void handleSetup(String cmd) {
        try {
            int size = Integer.parseInt(cmd);
            if (size >= 4 && size <= 16 && size % 2 == 0) {
                this.board = new Board(size);
                this.statusMessage = "";
            } else {
                statusMessage = "请输入 4 到 16 之间的偶数。";
            }
        } catch (NumberFormatException e) {
            statusMessage = "输入无效，请输入数字。";
        }
    }

    /**
     * 处理游戏过程中的落子命令。
     * @param cmd 用户输入的坐标字符串
     */
    private void handleMove(String cmd) {
        if (cmd.isEmpty()) return;
        Move move = Move.fromString(cmd);
        if (move == null) {
            statusMessage = "格式错误！请输入如 A1 的坐标。";
            isError = true;
            return;
        }

        int r = move.row(), c = move.col();
        if (board.isOutOfBounds(r, c)) {
            statusMessage = "坐标超出范围！";
            isError = true;
            return;
        }

        if (board.isOccupied(r, c)) {
            statusMessage = "该位置已有棋子！";
            isError = true;
            return;
        }

        if (board.place(r, c, currentTurn)) {
            statusMessage = "";
            isError = false;
            currentTurn = (currentTurn == Piece.BLACK) ? Piece.WHITE : Piece.BLACK;
        }
    }

    /**
     * 检查当前棋盘状态是否达成游戏结束条件。
     */
    private void checkGameOver() {
        if (board.isFull() && !gameOver) {
            gameOver = true;
            statusMessage = String.format("棋盘已满！黑: %d  白: %d。",
                    board.getCount(Piece.BLACK), board.getCount(Piece.WHITE));
            isError = false;
        }
    }

    /**
     * 释放终端资源
     * @throws Exception 如果停止屏幕时出现异常
     */
    @Override public void close() throws Exception { ui.close(); }
}