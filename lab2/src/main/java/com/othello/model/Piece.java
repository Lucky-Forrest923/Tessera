package com.othello.model;

/**
 * 棋子类型枚举
 */
public enum Piece {
    EMPTY(" · "),
    BLACK(" ● "),
    WHITE(" ● ");

    private final String display;

    Piece(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    /**
     * 获取当前棋子的对手颜色。
     * @return 对手棋子；若为空棋子则返回 EMPTY
     */
    public Piece opponent() {
        if (this == BLACK) {
            return WHITE;
        }
        if (this == WHITE) {
            return BLACK;
        }
        return EMPTY;
    }
}