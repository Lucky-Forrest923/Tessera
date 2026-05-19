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
}