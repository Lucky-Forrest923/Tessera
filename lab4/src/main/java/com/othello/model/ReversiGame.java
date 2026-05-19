package com.othello.model;

import com.othello.text.GameTexts;

import java.util.ArrayList;
import java.util.List;

public class ReversiGame extends OthelloGame {
    private static final int[][] DIRS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            { 0, -1},          { 0, 1},
            { 1, -1}, { 1, 0}, { 1, 1}
    };

    /**
     * 创建一局黑白棋。
     *
     * @param size 棋盘大小
     */
    public ReversiGame(int size) {
        super(size);
    }

    /**
     * 返回游戏类型标识。
     *
     * @return 游戏类型标识
     */
    @Override
    public String getType() {
        return GameTexts.TYPE_REVERSI;
    }

    /**
     * 执行一次黑白棋落子并翻转被夹住的棋子。
     *
     * @param position 目标位置
     * @return 如果落子有效则返回 {@code true}
     */
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

    /**
     * 获取当前所有合法落子位置。
     *
     * @return 合法落子列表
     */
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

    /**
     * 返回黑白棋的附加状态信息。
     *
     * @return 空字符串
     */
    @Override
    public String getGameSpecificStatus() {
        return "";
    }

    /**
     * 检查落子后是否已经进入结束状态或需要跳过回合。
     */
    private void checkGameStatus() {
        if (board.isFull()) {
            setOver();
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
            setOver();
            setCode(GameStatusCode.GAME_OVER_NO_MOVES);
        } else {
            setCode(GameStatusCode.PASS);
        }
    }

    /**
     * 计算某个落子点会翻转哪些棋子。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @param piece 当前落子的棋子颜色
     * @return 需要翻转的棋子列表
     */
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

    /**
     * 执行 pass 后的状态检查。
     */
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

    /**
     * 检查 pass 之后的游戏状态。
     */
    private void checkStatusAfterPass() {
        if (board.isFull()) {
            setOver();
            setCode(GameStatusCode.GAME_OVER_FULL);
            return;
        }
        if (!getLegalMoves().isEmpty()) {
            setCode(GameStatusCode.MOVE_ACCEPTED);
            return;
        }
        setOver();
        setCode(GameStatusCode.GAME_OVER_NO_MOVES);
    }
}

