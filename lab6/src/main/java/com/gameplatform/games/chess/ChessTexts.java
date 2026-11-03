package com.gameplatform.games.chess;

public final class ChessTexts {
    public static final String GAME_OVER = "游戏已结束！";
    public static final String INVALID_COORD = "坐标格式错误，请使用如 m a2 a3";
    public static final String OUT_OF_BOUNDS = "超出棋盘边界！";
    public static final String NO_PIECE = "该位置没有棋子";
    public static final String NOT_YOUR_TURN = "当前不是你的回合";
    public static final String ILLEGAL_MOVE = "非法走棋";
    public static final String PROMOTION_NOT_ALLOWED = "该走法不允许升变";
    public static final String INVALID_PROMOTION = "无效的升变类型";
    public static final String KING_IN_CHECK = "非法走棋：会导致王被将军";
    public static final String KING_CAPTURED = "对方的王被吃掉，游戏结束！";
    public static final String MOVE_SUCCESS = "行棋成功";
    public static final String COMMAND_MOVE = "- 行棋(move)：m a1 b2 [q/r/b/n] (兵升变选填)";

    public static String winnerLine(boolean whiteTurn) {
        return "结果：对方的王被吃掉，" + (whiteTurn ? "白方" : "黑方") + "获胜！";
    }

    private ChessTexts() {}
}

