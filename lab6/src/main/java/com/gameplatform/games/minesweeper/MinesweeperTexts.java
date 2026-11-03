package com.gameplatform.games.minesweeper;

public final class MinesweeperTexts {
    public static final String GAME_OVER = "游戏已结束";
    public static final String INVALID_COORD = "坐标格式错误，请使用 f a1";
    public static final String OUT_OF_BOUNDS = "坐标越界";
    public static final String CANNOT_OPEN_FLAGGED = "无法打开已插旗的位置";
    public static final String ALREADY_OPENED = "该位置已被打开";
    public static final String HIT_MINE = "踩雷了！游戏结束";
    public static final String ALL_SAFE_OPENED = "所有安全格已打开，游戏结束！";
    public static final String OPEN_SUCCESS = "打开成功";
    public static final String FLAG_SUCCESS = "插旗成功";
    public static final String UNFLAG_SUCCESS = "取消插旗成功";
    public static final String RESULT_WIN = "结果：你赢了！";
    public static final String RESULT_LOSE = "结果：你输了！";
    public static final String FIRST_OPEN_SAFE = "首开安全：第一次翻开必不为雷";
    public static final String COMMAND_OPEN = "- 打开：a1";
    public static final String COMMAND_FLAG = "- 插旗：f a1 / flag a1";
    public static final String COMMAND_NOTE = "- 相邻为0的格子暂时不会自动展开";

    public static String minesAndFlagsLine(int totalMines, int flagCount) {
        return "总雷数：" + totalMines + " 旗数：" + flagCount;
    }

    private MinesweeperTexts() {}
}

