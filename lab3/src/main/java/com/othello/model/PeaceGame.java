package com.othello.model;

import java.util.ArrayList;
import java.util.List;

public class PeaceGame extends BaseGame {

    public PeaceGame(int size) {
        super(size);
    }


    @Override
    public String getType() {
        return "peace";
    }

    @Override
    protected boolean doPlay(Position position) {
        board.setPiece(position.row(), position.col(), getTurn());
        switchTurn();
        if (board.isFull()) {
            setOver(true);
            setCode(GameStatusCode.GAME_OVER_FULL);
        } else {
            setCode(GameStatusCode.MOVE_ACCEPTED);
        }
        return true;
    }

    @Override
    public List<Position> getLegalMoves() {
        List<Position> moves = new ArrayList<>();
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                if (!board.isOccupied(r, c)) {
                    moves.add(new Position(r, c));
                }
            }
        }
        return moves;
    }
}