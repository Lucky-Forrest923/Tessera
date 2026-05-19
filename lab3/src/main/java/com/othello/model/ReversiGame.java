package com.othello.model;

import java.util.ArrayList;
import java.util.List;

public class ReversiGame extends BaseGame {
    private static final int[][] DIRS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            { 0, -1},          { 0, 1},
            { 1, -1}, { 1, 0}, { 1, 1}
    };

    public ReversiGame(int size) {
        super(size);
    }


    @Override
    public String getType() {
        return "reversi";
    }

    @Override
    protected boolean doPlay(Position position) {
        List<Position> flips = getFlips(position.row(), position.col(), getTurn());
        if (flips.isEmpty()) {
            setCode(GameStatusCode.ILLEGAL_MOVE);
            return false;
        }

        board.setPiece(position.row(), position.col(), getTurn());
        for (Position p : flips) {
            board.setPiece(p.row(), p.col(), getTurn());
        }

        switchTurn();
        checkGameStatus();
        return true;
    }

    @Override
    public void pass() {
        if (isOver()) {
            setCode(GameStatusCode.GAME_ALREADY_OVER);
            return;
        }
        if (!getLegalMoves().isEmpty()) {
            setCode(GameStatusCode.PASS_NOT_ALLOWED);
            return;
        }

        switchTurn();
        refreshScores();
        checkStatusAfterPass();
    }

    private void checkGameStatus() {
        if (board.isFull()) {
            setOver(true);
            setCode(GameStatusCode.GAME_OVER_FULL);
            return;
        }

        if (!getLegalMoves().isEmpty()) {
            setCode(GameStatusCode.MOVE_ACCEPTED);
            return;
        }

        // 当前玩家无合法落子，探测对手
        switchTurn();
        boolean opponentHasMoves = !getLegalMoves().isEmpty();
        switchTurn(); // 切回当前玩家

        if (!opponentHasMoves) {
            setOver(true);
            setCode(GameStatusCode.GAME_OVER_NO_MOVES);
        } else {
            setCode(GameStatusCode.PASS);
        }
    }

    private void checkStatusAfterPass() {
        if (board.isFull()) {
            setOver(true);
            setCode(GameStatusCode.GAME_OVER_FULL);
            return;
        }
        if (!getLegalMoves().isEmpty()) {
            setCode(GameStatusCode.MOVE_ACCEPTED);
            return;
        }
        // 对手也无合法落子
        setOver(true);
        setCode(GameStatusCode.GAME_OVER_NO_MOVES);
    }

    @Override
    public List<Position> getLegalMoves() {
        List<Position> moves = new ArrayList<>();
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                if (!board.isOccupied(r, c) && !getFlips(r, c, getTurn()).isEmpty()) {
                    moves.add(new Position(r, c));
                }
            }
        }
        return moves;
    }

    private List<Position> getFlips(int row, int col, Piece piece) {
        List<Position> flips = new ArrayList<>();
        if (board.isOutOfBounds(row, col) || board.isOccupied(row, col)) {
            return flips;
        }

        for (int[] d : DIRS) {
            List<Position> line = new ArrayList<>();
            int r = row + d[0];
            int c = col + d[1];

            while (!board.isOutOfBounds(r, c) && board.getPiece(r, c) == piece.opponent()) {
                line.add(new Position(r, c));
                r += d[0];
                c += d[1];
            }

            if (!line.isEmpty() && !board.isOutOfBounds(r, c) && board.getPiece(r, c) == piece) {
                flips.addAll(line);
            }
        }
        return flips;
    }

}