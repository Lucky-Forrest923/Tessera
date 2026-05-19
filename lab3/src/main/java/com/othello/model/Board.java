package com.othello.model;

public class Board {
    private final int size;
    private final Piece[][] grid;

    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive.");
        }
        this.size = size;
        this.grid = new Piece[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = Piece.EMPTY;
            }
        }
    }

    public int getSize() {
        return size;
    }

    public Piece getPiece(int row, int col) {
        requireInBounds(row, col);
        return grid[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        requireInBounds(row, col);
        grid[row][col] = piece;
    }

    public boolean isOutOfBounds(int row, int col) {
        return row < 0 || row >= size || col < 0 || col >= size;
    }

    public boolean isOccupied(int row, int col) {
        requireInBounds(row, col);
        return grid[row][col] != Piece.EMPTY;
    }

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

    public boolean isFull() {
        return getCount(Piece.EMPTY) == 0;
    }

    private void requireInBounds(int row, int col) {
        if (isOutOfBounds(row, col)) {
            throw new IllegalArgumentException("Out of bounds: (" + row + ", " + col + ")");
        }
    }
}