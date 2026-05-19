package com.othello.text;

import com.othello.model.GameStatusCode;

public final class GameTexts {
    public static final String TYPE_PEACE = "peace";
    public static final String TYPE_REVERSI = "reversi";
    public static final String TYPE_MINESWEEPER = "minesweeper";

    public static final String COMMAND_QUIT = "quit";
    public static final String COMMAND_PASS = "pass";
    public static final String COMMAND_SWITCH_SHORT = "s";
    public static final String COMMAND_SWITCH_LONG = "switch";
    public static final String COMMAND_FLAG_SHORT = "f";
    public static final String COMMAND_FLAG_LONG = "flag";

    public static final String APP_TITLE = "Othello Game Terminal";
    public static final String BOARD_BOX_TITLE = " BOARD ";
    public static final String STATUS_BOX_TITLE = " STATUS ";
    public static final String GAME_LIST_BOX_TITLE = " GAME LIST ";

    public static final String INPUT_PROMPT = "输入> ";
    public static final String SHORTCUTS = "快捷键：a1（落子） | f a1（插旗） | 1-9（切换） | peace/reversi/minesweeper（新建） | quit";

    public static final String GAME_OVER_BANNER = "！！！游戏结束！！！";
    public static final String LAST_MESSAGE_LABEL = "上一次消息：";
    public static final String MINESWEEPER_SAFE_HINT = "首开安全：第一次翻开必不为雷";

    /**
     * 私有构造方法，防止外部实例化该工具类。
     */
    private GameTexts() {
    }

    /**
     * 判断当前类型标识是否与预期类型一致。
     *
     * @param typeId 当前类型标识
     * @param expected 期望的类型标识
     * @return 如果两者一致则返回 {@code true}
     */
    public static boolean isType(String typeId, String expected) {
        return expected.equals(typeId);
    }

    /**
     * 返回游戏类型对应的显示名称。
     *
     * @param typeId 游戏类型标识
     * @return 对应的中文显示名称；若未知则返回原始标识
     */
    public static String gameDisplayName(String typeId) {
        if (TYPE_PEACE.equals(typeId)) {
            return "和平棋";
        }
        if (TYPE_REVERSI.equals(typeId)) {
            return "黑白棋";
        }
        if (TYPE_MINESWEEPER.equals(typeId)) {
            return "扫雷";
        }
        return typeId == null ? "" : typeId;
    }

    /**
     * 生成当前游戏信息的显示文本。
     *
     * @param currentNum 当前游戏编号
     * @param typeId 游戏类型标识
     * @return 当前游戏标签文本
     */
    public static String currentGameLabel(int currentNum, String typeId) {
        return "当前游戏：#" + currentNum + "（" + gameDisplayName(typeId) + "）";
    }

    /**
     * 生成新增游戏后的提示文本。
     *
     * @param typeId 游戏类型标识
     * @return 新增游戏提示文本
     */
    public static String addedGameMessage(String typeId) {
        return "已添加新的" + gameDisplayName(typeId) + "游戏";
    }

    /**
     * 生成切换游戏成功后的提示文本。
     *
     * @param number 目标游戏编号
     * @return 切换成功提示文本
     */
    public static String switchSuccessMessage(int number) {
        return "已切换到第 " + number + " 局";
    }

    /**
     * 返回不支持执行 pass 操作时的提示文本。
     *
     * @return 不支持 pass 的提示文本
     */
    public static String passUnsupportedMessage() {
        return unsupportedPassExceptionMessage();
    }

    /**
     * 返回 pass 成功后的提示文本。
     *
     * @return pass 成功提示文本
     */
    public static String passSuccessMessage() {
        return "Pass 成功，轮到下一位玩家。";
    }

    /**
     * 返回当前游戏不支持插旗时的提示文本。
     *
     * @return 不支持插旗的提示文本
     */
    public static String flagUnsupportedMessage() {
        return "当前游戏不支持插旗";
    }

    /**
     * 返回提示玩家执行 pass 的文本。
     *
     * @return pass 提示文本
     */
    public static String passHintMessage() {
        return "当前玩家无合法落子，请输入 pass。";
    }

    /**
     * 返回坐标格式错误时的提示文本。
     *
     * @return 坐标格式错误提示文本
     */
    public static String coordinateFormatErrorMessage() {
        return "坐标格式错误，请使用 a1 或 1a";
    }

    /**
     * 返回通用输入格式错误时的提示文本。
     *
     * @return 输入格式错误提示文本
     */
    public static String inputFormatErrorMessage() {
        return "输入格式有误！请输入正确的位置格式，如 D4";
    }

    /**
     * 返回棋盘编号越界时的提示文本。
     *
     * @param max 当前最大游戏局数
     * @return 编号越界提示文本
     */
    public static String boardNumberOutOfRangeMessage(int max) {
        return "游戏编号超出范围：当前共有 " + max + " 局，请输入 1-" + max + "。";
    }

    /**
     * 根据状态码返回黑白棋的状态提示文本。
     *
     * @param code 状态码
     * @return 对应的黑白棋状态文本
     */
    public static String othelloStatusMessage(GameStatusCode code) {
        return switch (code) {
            case GAME_START -> "黑白棋游戏开始！";
            case GAME_ALREADY_OVER -> "游戏已结束！";
            case INVALID_COORDINATE -> "无效坐标！";
            case OUT_OF_BOUNDS -> "超出棋盘范围！";
            case OCCUPIED -> "该位置已被占用！";
            case ILLEGAL_MOVE -> "非法落子！";
            case PASS_NOT_ALLOWED -> "当前不支持执行 Pass！";
            case MOVE_ACCEPTED -> "落子成功！";
            case GAME_OVER_FULL -> "棋盘已满，游戏结束";
            case GAME_OVER_NO_MOVES -> "双方均无合法落子，游戏结束";
            default -> "";
        };
    }

    /**
     * 根据状态码返回扫雷的状态提示文本。
     *
     * @param code 状态码
     * @return 对应的扫雷状态文本
     */
    public static String minesweeperStatusMessage(GameStatusCode code) {
        return switch (code) {
            case FLAG_SUCCESS -> "插旗成功";
            case UNFLAG_SUCCESS -> "取消插旗成功";
            case INVALID_COORDINATE -> "无效坐标";
            case OUT_OF_BOUNDS -> "坐标越界";
            case OCCUPIED -> "该位置已被打开或插旗";
            case GAME_ALREADY_OVER -> "游戏已结束";
            case CANNOT_OPEN_FLAGGED -> "无法打开已插旗的位置";
            case MINE_HIT -> "踩雷了！游戏结束";
            case GAME_OVER_ALL_SAFE_OPENED -> "恭喜！你赢了！";
            case MOVE_ACCEPTED -> "打开成功";
            default -> "";
        };
    }

    /**
     * 返回游戏结束原因的简短描述。
     *
     * @param code 状态码
     * @return 对应的结束原因文本；如果没有原因则返回空字符串
     */
    public static String gameOverReason(GameStatusCode code) {
        return switch (code) {
            case GAME_OVER_FULL -> "棋盘已满";
            case GAME_OVER_NO_MOVES -> "双方均无合法落子";
            case MINE_HIT -> "踩到地雷";
            case GAME_OVER_ALL_SAFE_OPENED -> "所有安全格已打开";
            default -> "";
        };
    }

    /**
     * 返回带有“原因：”前缀的游戏结束原因文本。
     *
     * @param code 状态码
     * @return 带前缀的原因文本；如果没有原因则返回空字符串
     */
    public static String gameOverReasonLabel(GameStatusCode code) {
        String reason = gameOverReason(code);
        return reason.isEmpty() ? "" : "原因：" + reason;
    }

    /**
     * 生成黑白棋最终比分的显示文本。
     *
     * @param black 黑方得分
     * @param white 白方得分
     * @return 最终比分文本
     */
    public static String reversiFinalScore(int black, int white) {
        return "最终比分：黑 / 白 = " + black + " / " + white;
    }

    /**
     * 根据最终比分生成黑白棋的胜负结果文本。
     *
     * @param black 黑方得分
     * @param white 白方得分
     * @return 胜负结果文本
     */
    public static String reversiResult(int black, int white) {
        return black > white
                ? "结果：黑方获胜"
                : (white > black ? "结果：白方获胜" : "结果：平局");
    }

    /**
     * 根据状态码生成扫雷的胜负结果文本。
     *
     * @param code 状态码
     * @return 扫雷胜负结果文本；如果不是结束状态则返回空字符串
     */
    public static String minesweeperResult(GameStatusCode code) {
        return switch (code) {
            case GAME_OVER_ALL_SAFE_OPENED -> "结果：你赢了！";
            case MINE_HIT -> "结果：你输了！";
            default -> "";
        };
    }

    /**
     * 生成扫雷的状态栏文本。
     *
     * @param totalMines 地雷总数
     * @param flagCount 当前旗子数量
     * @return 扫雷状态栏文本
     */
    public static String minesweeperGameStatus(int totalMines, int flagCount) {
        return "总雷数：" + totalMines + " 旗数：" + flagCount;
    }

    /**
     * 返回棋盘大小非法时的提示文本。
     *
     * @return 棋盘大小非法提示文本
     */
    public static String boardSizeMustBePositiveMessage() {
        return "棋盘大小必须大于 0。";
    }

    /**
     * 返回坐标越界时的提示文本。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 坐标越界提示文本
     */
    public static String outOfBoundsMessage(int row, int col) {
        return "坐标越界：(" + row + ", " + col + ")";
    }

    /**
     * 返回无效数字输入时的提示文本。
     *
     * @param n 输入的数字
     * @return 无效数字提示文本
     */
    public static String invalidCellNumberMessage(int n) {
        return "无效数字：" + n;
    }

    /**
     * 返回当前游戏不支持 pass 操作时的异常提示文本。
     *
     * @return 不支持 pass 的异常提示文本
     */
    public static String unsupportedPassExceptionMessage() {
        return "当前游戏不支持 pass";
    }
}

