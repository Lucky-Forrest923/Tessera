package com.gameplatform.games.chess;

public enum ChessPiece {
    EMPTY(Color.NONE),
    BQUEEN(Color.BLACK), BKING(Color.BLACK), BROOK(Color.BLACK),
    BBISHOP(Color.BLACK), BKNIGHT(Color.BLACK), BPAWN(Color.BLACK),
    WQUEEN(Color.WHITE), WKING(Color.WHITE), WROOK(Color.WHITE),
    WBISHOP(Color.WHITE), WKNIGHT(Color.WHITE), WPAWN(Color.WHITE);

    private final Color color;

    ChessPiece(Color color) {
        this.color = color;
    }

    public boolean isWhite() {
        return color == Color.WHITE;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean isQueen() {
        return this == BQUEEN || this == WQUEEN;
    }

    public boolean isKing() {
        return this == BKING || this == WKING;
    }

    public boolean isRook() {
        return this == BROOK || this == WROOK;
    }

    public boolean isBishop() {
        return this == BBISHOP || this == WBISHOP;
    }

    public boolean isKnight() {
        return this == BKNIGHT || this == WKNIGHT;
    }

    public boolean isPawn() {
        return this == BPAWN || this == WPAWN;
    }

    public char displayChessPiece() {
        return switch (this) {
            case BQUEEN -> 'Q';
            case BKING -> 'K';
            case BROOK -> 'R';
            case BBISHOP -> 'B';
            case BKNIGHT -> 'N';
            case BPAWN -> 'P';
            case WQUEEN -> 'q';
            case WKING -> 'k';
            case WROOK -> 'r';
            case WBISHOP -> 'b';
            case WKNIGHT -> 'n';
            case WPAWN -> 'p';
            case EMPTY -> '.';
        };
    }
}




