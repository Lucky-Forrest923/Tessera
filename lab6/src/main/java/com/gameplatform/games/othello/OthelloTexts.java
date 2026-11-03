package com.gameplatform.games.othello;

public final class OthelloTexts {
    public static final String GAME_OVER = "游戏已结束！";
    public static final String OUT_OF_BOUNDS = "超出棋盘范围！";
    public static final String OCCUPIED = "该位置已被占用！";
    public static final String SCORE_PREFIX = "当前比分：黑 / 白 = ";
    public static final String RESULT_BLACK_WIN = "结果：黑方获胜";
    public static final String RESULT_WHITE_WIN = "结果：白方获胜";
    public static final String RESULT_DRAW = "结果：平局";

    public static final String INVALID_MOVE = "非法落子！";
    public static final String PASS_NOT_ALLOWED = "当前有合法落子，不允许 Pass！";
    public static final String PASS_PROMPT = "当前玩家无合法落子，请输入 pass。";
    public static final String PASS_SUCCESS = "已跳过";
    public static final String BOARD_FULL_OVER = "棋盘已满，游戏结束";
    public static final String NO_MOVES_OVER = "双方均无合法落子，游戏结束";
    public static final String MOVE_SUCCESS = "落子成功";
    public static final String MOVE_SUCCESS_EXCLAIM = "落子成功！";

    public static String scoreLine(int black, int white) {
        return SCORE_PREFIX + black + " / " + white;
    }

    public static String boardSizeMustBePositiveMessage() {
        return "棋盘大小必须为正数";
    }

    public static String outOfBoundsMessage(int row, int col) {
        return "坐标越界: (" + row + ", " + col + ")";
    }

    private OthelloTexts() {}
}
