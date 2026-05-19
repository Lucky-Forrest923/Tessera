package com.othello.model;

/**
 * 坐标值对象。
 * 用于表示棋盘上的行列索引。
 */
public class Position {
    private final int row;
    private final int col;

    /**
     * 构造坐标对象。
     * @param row 行索引（从 0 开始）
     * @param col 列索引（从 0 开始）
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /** @return 行索引 */
    public int row() {
        return row;
    }

    /** @return 列索引 */
    public int col() {
        return col;
    }

}

