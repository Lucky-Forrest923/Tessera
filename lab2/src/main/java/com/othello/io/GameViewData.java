package com.othello.io;

import com.othello.model.Board;

/**
 * 从控制器传递到前端的不可变视图数据。
 */
public final class GameViewData {
    private final Board board;
    private final String currentPlayer;
    private final String input;
    private final String statusMessage;
    private final boolean gameOver;
    private final boolean error;
    private final int blackCount;
    private final int whiteCount;
    private final int currentBoardNumber;
    private final int totalBoards;

    /**
     * 构造视图数据对象。
     * @param board 棋盘对象
     * @param currentPlayer 当前玩家
     * @param input 当前输入
     * @param statusMessage 状态信息
     * @param gameOver 是否游戏结束
     * @param error 是否为错误信息
     * @param blackCount 黑棋数量
     * @param whiteCount 白棋数量
     * @param currentBoardNumber 当前棋盘编号
     * @param totalBoards 棋盘总数
     */
    public GameViewData(Board board,
                        String currentPlayer,
                        String input,
                        String statusMessage,
                        boolean gameOver,
                        boolean error,
                        int blackCount,
                        int whiteCount,
                        int currentBoardNumber,
                        int totalBoards) {
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.input = input;
        this.statusMessage = statusMessage;
        this.gameOver = gameOver;
        this.error = error;
        this.blackCount = blackCount;
        this.whiteCount = whiteCount;
        this.currentBoardNumber = currentBoardNumber;
        this.totalBoards = totalBoards;
    }

    /** 获取棋盘对象 */
    public Board board() {
        return board;
    }

    /** 获取当前玩家 */
    public String currentPlayer() {
        return currentPlayer;
    }

    /** 获取当前输入 */
    public String input() {
        return input;
    }

    /** 获取状态信息 */
    public String statusMessage() {
        return statusMessage;
    }

    /** 是否游戏结束 */
    public boolean gameOver() {
        return gameOver;
    }

    /** 是否为错误信息 */
    public boolean error() {
        return error;
    }

    /** 获取黑棋数量 */
    public int blackCount() {
        return blackCount;
    }

    /** 获取白棋数量 */
    public int whiteCount() {
        return whiteCount;
    }

    /** 获取当前棋盘编号 */
    public int currentBoardNumber() {
        return currentBoardNumber;
    }

    /** 获取棋盘总数 */
    public int totalBoards() {
        return totalBoards;
    }
}
