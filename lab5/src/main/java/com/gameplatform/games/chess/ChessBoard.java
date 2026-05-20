package com.gameplatform.games.chess;

public class ChessBoard {
    private final int size;
    private final ChessPiece[][] cells;

    public ChessBoard(int size) {
        this.size = size;
        this.cells = new ChessPiece[size][size];
        clear();
    }

    public int getSize() {
        return size;
    }

    public ChessPiece get(int row, int col) {
        return cells[row][col];
    }

    public void set(int row, int col, ChessPiece piece) {
        cells[row][col] = piece;
    }

    public boolean isOutOfBounds(int row, int col) {
        return row < 0 || row >= size || col < 0 || col >= size;
    }

    public ChessPiece[][] copy() {
        ChessPiece[][] copy = new ChessPiece[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(cells[i], 0, copy[i], 0, size);
        }
        return copy;
    }

    public void initializeStandardSetup() {
        clear();
        if (size >= 8) {
            // Black pieces — back rank at row 0 (top of display)
            cells[0][0] = ChessPiece.BROOK;
            cells[0][1] = ChessPiece.BKNIGHT;
            cells[0][2] = ChessPiece.BBISHOP;
            cells[0][3] = ChessPiece.BQUEEN;
            cells[0][4] = ChessPiece.BKING;
            cells[0][5] = ChessPiece.BBISHOP;
            cells[0][6] = ChessPiece.BKNIGHT;
            cells[0][7] = ChessPiece.BROOK;
            for (int c = 0; c < 8; c++) cells[1][c] = ChessPiece.BPAWN;
            // White pieces — back rank at row 7 (bottom of display)
            for (int c = 0; c < 8; c++) cells[6][c] = ChessPiece.WPAWN;
            cells[7][0] = ChessPiece.WROOK;
            cells[7][1] = ChessPiece.WKNIGHT;
            cells[7][2] = ChessPiece.WBISHOP;
            cells[7][3] = ChessPiece.WQUEEN;
            cells[7][4] = ChessPiece.WKING;
            cells[7][5] = ChessPiece.WBISHOP;
            cells[7][6] = ChessPiece.WKNIGHT;
            cells[7][7] = ChessPiece.WROOK;
        }
    }

    private void clear() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                cells[r][c] = ChessPiece.EMPTY;
            }
        }
    }
}

