package com.gameplatform.games.chess;

public enum ChessPiece {
    EMPTY(PieceColor.NONE),
    BQUEEN(PieceColor.BLACK), BKING(PieceColor.BLACK), BROOK(PieceColor.BLACK),
    BBISHOP(PieceColor.BLACK), BKNIGHT(PieceColor.BLACK), BPAWN(PieceColor.BLACK),
    WQUEEN(PieceColor.WHITE), WKING(PieceColor.WHITE), WROOK(PieceColor.WHITE),
    WBISHOP(PieceColor.WHITE), WKNIGHT(PieceColor.WHITE), WPAWN(PieceColor.WHITE);

    private final PieceColor color;

    ChessPiece(PieceColor color) {
        this.color = color;
    }

    public boolean isWhite() {
        return color == PieceColor.WHITE;
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