package com.othello.model;

/**
 * 对局状态类。
 * 负责维护回合、终局标记、状态码和当前比分。
 */
public class GameState {
    private Piece currentTurn = Piece.BLACK;
    private boolean gameOver;
    private GameStatusCode statusCode = GameStatusCode.NONE;
    private int blackCount;
    private int whiteCount;

    /** @return 当前回合执子方 */
    public Piece getCurrentTurn() {
        return currentTurn;
    }

    /** @param currentTurn 设置当前回合执子方 */
    public void setCurrentTurn(Piece currentTurn) {
        this.currentTurn = currentTurn;
    }

    /** @return 是否游戏结束 */
    public boolean isGameOver() {
        return gameOver;
    }

    /** @param gameOver 设置游戏结束标记 */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * 判断当前状态是否为错误。
     * @return true 表示当前状态是错误状态
     */
    public boolean isError() {
        return statusCode != null && statusCode.isError();
    }

    /** @return 状态码 */
    public GameStatusCode getStatusCode() {
        return statusCode;
    }

    /** @param statusCode 设置状态码 */
    public void setStatusCode(GameStatusCode statusCode) {
        this.statusCode = statusCode == null ? GameStatusCode.NONE : statusCode;
    }

    /** @return 黑棋数量 */
    public int getBlackCount() {
        return blackCount;
    }

    /** @return 白棋数量 */
    public int getWhiteCount() {
        return whiteCount;
    }

    /**
     * 根据棋盘刷新双方棋子数量。
     * @param board 棋盘
     */
    public void refreshCounts(Board board) {
        blackCount = board.getCount(Piece.BLACK);
        whiteCount = board.getCount(Piece.WHITE);
    }
}