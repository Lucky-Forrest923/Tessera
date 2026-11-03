package com.gameplatform.games.chess;

import com.gameplatform.model.Position;

class ChessMoveCandidate {
    final ChessPiece piece;
    final Position from;
    final Position to;
    final ChessPiece captured;
    final boolean doublePawnStep;
    final boolean enPassant;
    final boolean castling;
    final boolean promotion;
    ChessPiece promotionType;

    ChessMoveCandidate(ChessPiece piece, Position from, Position to, ChessPiece captured,
                       boolean doublePawnStep, boolean enPassant, boolean castling,
                       boolean promotion, ChessPiece promotionType) {
        this.piece = piece;
        this.from = from;
        this.to = to;
        this.captured = captured;
        this.doublePawnStep = doublePawnStep;
        this.enPassant = enPassant;
        this.castling = castling;
        this.promotion = promotion;
        this.promotionType = promotionType;
    }
}
