package com.gameplatform.games.othello;

public class Board {
    private final int size;
    private final Piece[][] grid;

    /**
     * 创建一个指定大小的棋盘，并初始化为空棋盘。
     *
     * @param size 棋盘大小
     */
    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException(OthelloTexts.boardSizeMustBePositiveMessage());
        }
        this.size = size;
        this.grid = new Piece[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = Piece.EMPTY;
            }
        }
    }

    /**
     * 获取棋盘边长。
     *
     * @return 棋盘大小
     */
    public int getSize() {
        return size;
    }

    /**
     * 获取指定位置的棋子。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 指定位置的棋子
     */
    public Piece getPiece(int row, int col) {
        requireInBounds(row, col);
        return grid[row][col];
    }

    /**
     * 设置指定位置的棋子。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @param piece 要放置的棋子
     */
    public void setPiece(int row, int col, Piece piece) {
        requireInBounds(row, col);
        grid[row][col] = piece;
    }

    /**
     * 判断指定位置是否越界。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 如果越界则返回 {@code true}
     */
    public boolean isOutOfBounds(int row, int col) {
        return row < 0 || row >= size || col < 0 || col >= size;
    }

    /**
     * 判断指定位置是否已被占用。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 如果该位置不是空格则返回 {@code true}
     */
    public boolean isOccupied(int row, int col) {
        requireInBounds(row, col);
        return grid[row][col] != Piece.EMPTY;
    }

    /**
     * 统计指定棋子在棋盘上的数量。
     *
     * @param piece 目标棋子
     * @return 棋子数量
     */
    public int getCount(Piece piece) {
        int count = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] == piece) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 判断棋盘是否已满。
     *
     * @return 如果没有空位则返回 {@code true}
     */
    public boolean isFull() {
        return getCount(Piece.EMPTY) == 0;
    }

    /**
     * 校验坐标是否在棋盘范围内。
     *
     * @param row 行坐标
     * @param col 列坐标
     */
    private void requireInBounds(int row, int col) {
        if (isOutOfBounds(row, col)) {
            throw new IllegalArgumentException(OthelloTexts.outOfBoundsMessage(row, col));
        }
    }
}
