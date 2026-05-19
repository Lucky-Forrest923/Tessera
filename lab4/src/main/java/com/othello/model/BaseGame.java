package com.othello.model;

import java.util.List;

public abstract class BaseGame {
    protected final int size;
    private boolean over = false;
    protected GameStatusCode code = GameStatusCode.NONE;

    /**
     * 创建一个指定大小的游戏实例。
     *
     * @param size 棋盘大小
     */
    public BaseGame(int size) {
        this.size = size;
    }

    /**
     * 获取棋盘大小。
     *
     * @return 棋盘大小
     */
    public int getSize() {
        return size;
    }

    /**
     * 判断当前对局是否已经结束。
     *
     * @return 如果已经结束则返回 {@code true}
     */
    public boolean isOver() {
        return over;
    }

    /**
     * 将当前对局标记为结束。
     */
    public void setOver() {
        over = true;
    }

    /**
     * 获取当前状态码。
     *
     * @return 当前状态码
     */
    public GameStatusCode getCode() {
        return code;
    }

    /**
     * 设置当前状态码。
     *
     * @param code 目标状态码
     */
    public void setCode(GameStatusCode code) {
        // 如果当前已有一个最终结束码（例如踩雷或任意 GAME_OVER_*），则不允许后续覆盖该结束提示。
        if (isFinalCode(this.code)) {
            return;
        }
        this.code = code;
    }

    /**
     * 判断某个状态码是否代表游戏的最终结束状态。
     *
     * @param code 待判断的状态码
     * @return 如果是最终结束状态则返回 {@code true}
     */
    private static boolean isFinalCode(GameStatusCode code) {
        return switch (code) {
            case MINE_HIT, GAME_OVER_ALL_SAFE_OPENED, GAME_OVER_FULL, GAME_OVER_NO_MOVES -> true;
            default -> false;
        };
    }

    /**
     * 获取当前轮到的玩家。
     *
     * <p>默认返回黑方，供非黑白棋模式的 UI 安全调用。</p>
     *
     * @return 当前玩家
     */
    public Piece getTurn() {
        return Piece.BLACK;
    }

    /**
     * 获取黑方当前得分。
     *
     * @return 黑方得分
     */
    public int getBlackScore() {
        return 0;
    }

    /**
     * 获取白方当前得分。
     *
     * @return 白方得分
     */
    public int getWhiteScore() {
        return 0;
    }

    /**
     * 获取当前游戏的棋盘对象。
     *
     * @return 棋盘对象；若当前模式不使用棋盘则可能返回 {@code null}
     */
    public Board getBoard() {
        return null;
    }

    /**
     * 判断最近一次操作是否属于错误或非法操作。
     *
     * @return 如果当前状态代表错误则返回 {@code true}
     */
    public boolean isStatusError() {
        return switch (code) {
            case NONE, GAME_START, MOVE_ACCEPTED, FLAG_SUCCESS, UNFLAG_SUCCESS,
                 GAME_OVER_ALL_SAFE_OPENED, GAME_OVER_FULL, GAME_OVER_NO_MOVES, PASS -> false;
            case GAME_ALREADY_OVER, INVALID_COORDINATE, OUT_OF_BOUNDS, OCCUPIED,
                 ILLEGAL_MOVE, PASS_NOT_ALLOWED, MINE_HIT, CANNOT_OPEN_FLAGGED -> true;
        };
    }

    /**
     * 返回游戏类型标识。
     *
     * @return 游戏类型标识
     */
    public abstract String getType();

    /**
     * 返回当前状态消息。
     *
     * @return 状态消息文本
     */
    public abstract String getStatusMessage();

    /**
     * 执行一次落子或翻开操作。
     *
     * @param position 目标位置
     */
    public abstract void play(Position position);

    /**
     * 获取当前局面的所有合法动作。
     *
     * @return 合法动作列表
     */
    public abstract List<Position> getLegalMoves();

    /**
     * 返回指定坐标在界面中的显示字符。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 单元格显示字符
     */
    public abstract String getCellChar(int row, int col);

    /**
     * 返回游戏特有状态信息。
     *
     * @return 游戏特有状态文本
     */
    public abstract String getGameSpecificStatus();
}