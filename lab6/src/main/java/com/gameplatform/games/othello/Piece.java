package com.gameplatform.games.othello;

public enum Piece {
    EMPTY,
    BLACK,
    WHITE;

    /**
     * 返回当前棋子的对手棋子。
     *
     * @return 对手棋子；空位则返回 {@code EMPTY}
     */
    public Piece opponent() {
        return switch (this) {
            case BLACK -> WHITE;
            case WHITE -> BLACK;
            default -> EMPTY;
        };
    }
}