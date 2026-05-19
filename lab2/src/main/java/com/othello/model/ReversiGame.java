package com.othello.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑白棋规则与对局状态类。
 * 负责合法落子判断、翻子、弃权与终局判定。
 */
public class ReversiGame {
    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
    };

    private final Board board;
    private final GameState state;

    /**
     * 创建对局并初始化当前执子方。
     * @param boardSize 棋盘大小
     */
    public ReversiGame(int boardSize) {
        this.board = new Board(boardSize);
        this.state = new GameState();
        state.setCurrentTurn(Piece.BLACK);
        state.setStatusCode(GameStatusCode.GAME_START);
        state.refreshCounts(board);
    }

    /** @return 对局状态 */
    public GameState getState() {
        return state;
    }

    /** @return 棋盘 */
    public Board getBoard() {
        return board;
    }

    /**
     * 处理一次落子。
     * @param position 坐标
     */
    public void play(Position position) {
        if (state.isGameOver()) {
            state.setStatusCode(GameStatusCode.GAME_ALREADY_OVER);
            return;
        }

        if (position == null) {
            state.setStatusCode(GameStatusCode.INVALID_COORDINATE);
            return;
        }

        int row = position.row();
        int col = position.col();
        if (board.isOutOfBounds(row, col)) {
            state.setStatusCode(GameStatusCode.OUT_OF_BOUNDS);
            return;
        }

        if (board.isOccupied(row, col)) {
            state.setStatusCode(GameStatusCode.OCCUPIED);
            return;
        }

        List<Position> flips = collectFlips(row, col, state.getCurrentTurn());
        if (flips.isEmpty()) {
            state.setStatusCode(GameStatusCode.ILLEGAL_MOVE);
            return;
        }

        board.setPiece(row, col, state.getCurrentTurn());
        for (Position p : flips) {
            board.setPiece(p.row(), p.col(), state.getCurrentTurn());
        }

        state.refreshCounts(board);
        state.setStatusCode(GameStatusCode.MOVE_ACCEPTED);
        state.setCurrentTurn(state.getCurrentTurn().opponent());
        updateTurnAndGameOver();
    }

    /**
     * 每回合刷新可落子状态：无可落子自动弃权，双方都无可落子则结束。
     */
    public void updateTurnAndGameOver() {
        if (state.isGameOver()) {
            return;
        }

        state.refreshCounts(board);
        if (board.isFull()) {
            state.setGameOver(true);
            state.setStatusCode(GameStatusCode.GAME_OVER_FULL);
            return;
        }

        if (hasLegalMove(state.getCurrentTurn())) {
            return;
        }

        Piece next = state.getCurrentTurn().opponent();
        if (hasLegalMove(next)) {
            state.setStatusCode(GameStatusCode.PASS);
            state.setCurrentTurn(next);
            return;
        }

        state.setGameOver(true);
        state.setStatusCode(GameStatusCode.GAME_OVER_NO_MOVES);
    }

    /**
     * 判断指定执子方是否存在合法落子。
     * @param piece 执子方
     * @return 存在合法落子返回 true
     */
    public boolean hasLegalMove(Piece piece) {
        if (piece == null || piece == Piece.EMPTY) {
            return false;
        }

        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                if (board.isOccupied(r, c)) {
                    continue;
                }
                if (!collectFlips(r, c, piece).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Position> collectFlips(int row, int col, Piece piece) {
        List<Position> flips = new ArrayList<>();
        Piece opponent = piece.opponent();

        for (int[] dir : DIRECTIONS) {
            int dr = dir[0];
            int dc = dir[1];
            int nr = row + dr;
            int nc = col + dc;
            List<Position> line = new ArrayList<>();

            while (!board.isOutOfBounds(nr, nc) && board.getPiece(nr, nc) == opponent) {
                line.add(new Position(nr, nc));
                nr += dr;
                nc += dc;
            }

            if (!line.isEmpty() && !board.isOutOfBounds(nr, nc) && board.getPiece(nr, nc) == piece) {
                flips.addAll(line);
            }
        }

        return flips;
    }

}

