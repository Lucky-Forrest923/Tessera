package com.othello.model;

/**
 * 棋盘模型类。
 * 负责维护棋盘格点数据与基础状态查询。
 */
public class Board {
    private final int size;
    private final Piece[][] grid;

    /**
     * 初始化棋盘并设置开局棋子
     * @param size 棋盘的边长
     * @throws IllegalArgumentException 如果 size 不是偶数或小于 2
     */
    public Board(int size) {
        if (size < 2 || size % 2 != 0) {
            throw new IllegalArgumentException("棋盘大小必须为不小于 2 的偶数");
        }

        this.size = size;
        this.grid = new Piece[size][size];

        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) grid[i][j] = Piece.EMPTY;

        int mid = size / 2;
        grid[mid - 1][mid - 1] = Piece.WHITE;
        grid[mid][mid] = Piece.WHITE;
        grid[mid - 1][mid] = Piece.BLACK;
        grid[mid][mid - 1] = Piece.BLACK;
    }

    /**
     * 校验坐标是否在棋盘有效数组范围内。
     * @param r 行索引
     * @param c 列索引
     * @return true 表示坐标有效
     */
    public boolean isOutOfBounds(int r, int c) {
        return r < 0 || r >= size || c < 0 || c >= size;
    }

    /**
     * 校验指定位置是否已经放置过棋子。
     * @param r 行索引
     * @param c 列索引
     * @return true 表示该位置已有棋子
     */
    public boolean isOccupied(int r, int c) {
        if (isOutOfBounds(r, c)) {
            return false;
        }
        return grid[r][c] != Piece.EMPTY;
    }

    /**
     * 设置指定位置的棋子。
     * 由规则对象在合法性校验通过后调用。
     */
    public void setPiece(int r, int c, Piece piece) {
        if (piece == null) {
            throw new IllegalArgumentException("棋子类型不能为空");
        }
        if (isOutOfBounds(r, c)) {
            throw new IllegalArgumentException("坐标超出棋盘范围");
        }
        grid[r][c] = piece;
    }


    /**
     * 统计棋盘上指定类型棋子的总数。
     * @param piece 要统计的棋子类型
     * @return 棋子数量
     */
    public int getCount(Piece piece) {
        int count = 0;
        for (Piece[] row : grid)
            for (Piece cell : row) if (cell == piece) count++;
        return count;
    }

    /**
     * 获取指定坐标位置的棋子状态。
     * @param r 行索引
     * @param c 列索引
     * @return 棋子类型
     */
    public Piece getPiece(int r, int c) {
        if (isOutOfBounds(r, c)) {
            throw new IllegalArgumentException("坐标超出棋盘范围");
        }
        return grid[r][c];
    }

    /**
     * 获取棋盘边长。
     * @return 棋盘大小
     */
    public int getSize() { return size; }

    /**
     * 判断棋盘是否已经没有空位。
     * @return true 表示棋盘已满
     */
    public boolean isFull() {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (grid[i][j] == Piece.EMPTY) return false;
        return true;
    }
}