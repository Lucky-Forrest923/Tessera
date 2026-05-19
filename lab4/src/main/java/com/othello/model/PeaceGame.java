package com.othello.model;

import com.othello.text.GameTexts;

import java.util.ArrayList;
import java.util.List;

public class PeaceGame extends OthelloGame {

    /**
     * 创建一局和平棋。
     *
     * @param size 棋盘大小
     */
    public PeaceGame(int size) {
        super(size);
    }

    /**
     * 返回游戏类型标识。
     *
     * @return 游戏类型标识
     */
    @Override
    public String getType() {
        return GameTexts.TYPE_PEACE;
    }

    /**
     * 执行一次和平棋落子。
     *
     * @param position 目标位置
     * @return 始终返回 {@code true}
     */
    @Override
    protected boolean doPlay(Position position) {
        board.setPiece(position.row(), position.col(), getTurn());
        switchTurn();
        if (board.isFull()) {
            setOver();
            setCode(GameStatusCode.GAME_OVER_FULL);
        } else {
            setCode(GameStatusCode.MOVE_ACCEPTED);
        }
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
                if (!board.isOccupied(r, c)) {
                    moves.add(new Position(r, c));
                }
            }
        }
        return moves;
    }

    /**
     * 返回和平棋的附加状态信息。
     *
     * @return 空字符串
     */
    @Override
    public String getGameSpecificStatus() {
        return "";
    }
}