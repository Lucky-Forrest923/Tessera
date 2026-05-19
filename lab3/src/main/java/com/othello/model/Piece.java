package com.othello.model;

public enum Piece {
    EMPTY,
    BLACK,
    WHITE;

    public Piece opponent() {
        return switch (this) {
            case BLACK -> WHITE;
            case WHITE -> BLACK;
            default -> EMPTY;
        };
    }
}