package com.gameplatform.games.chess;

import com.gameplatform.model.Position;

public class ChessMove {
    final Position from;
    final Position to;
    final ChessPiece moved;
    final ChessPiece captured;
    final boolean doublePawnStep;
    final boolean enPassant;
    final boolean castling;
    final ChessPiece promotion;

    ChessMove(Position from, Position to, ChessPiece moved, ChessPiece captured,
              boolean doublePawnStep, boolean enPassant, boolean castling, ChessPiece promotion) {
        this.from = from;
        this.to = to;
        this.moved = moved;
        this.captured = captured;
        this.doublePawnStep = doublePawnStep;
        this.enPassant = enPassant;
        this.castling = castling;
        this.promotion = promotion;
    }
}
