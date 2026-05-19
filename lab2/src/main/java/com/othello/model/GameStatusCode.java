package com.othello.model;

/**
 * 对局状态码。
 * 每个状态码自带消息模板和错误标记。
 */
public enum GameStatusCode {
    NONE(false, ""),
    GAME_START(false, "游戏开始！黑棋先行。"),
    GAME_ALREADY_OVER(false, "游戏已结束。请输入退出结束。"),
    INVALID_COORDINATE(true, "格式错误！请输入如 A1 的坐标。"),
    OUT_OF_BOUNDS(true, "坐标超出范围！"),
    OCCUPIED(true, "该位置已有棋子！"),
    ILLEGAL_MOVE(true, "非法落子！该位置无法夹住对手棋子。"),
    MOVE_ACCEPTED(false, "落子成功。"),
    PASS(false, "当前玩家无合法落子，自动弃权。"),
    GAME_OVER_FULL(false, "棋盘已满，游戏结束。"),
    GAME_OVER_NO_MOVES(false, "双方均无合法落子，游戏结束。");

    private final boolean isError;
    private final String messageTemplate;

    GameStatusCode(boolean isError, String messageTemplate) {
        this.isError = isError;
        this.messageTemplate = messageTemplate;
    }

    /**
     * 判断当前状态码是否表示错误。
     * @return true 表示错误状态
     */
    public boolean isError() {
        return isError;
    }

    /**
     * 获取消息模板。
     * @return 消息模板字符串
     */
    public String getMessageTemplate() {
        return messageTemplate;
    }

}