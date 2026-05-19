package com.othello.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 多对局管理器。
 * 负责维护多个独立棋盘，并支持切换、增加、删除棋盘。
 */
public class MultiBoardManager {
    private final List<ReversiGame> boards = new ArrayList<>();
    private int activeBoardIndex;

    /**
     * 构造固定数量棋盘。
     * @param boardCount 棋盘数量
     * @param boardSize 棋盘大小
     */
    public MultiBoardManager(int boardCount, int boardSize) {
        if (boardCount < 1) {
            throw new IllegalArgumentException("棋盘数量至少为 1");
        }

        for (int i = 0; i < boardCount; i++) {
            boards.add(new ReversiGame(boardSize));
        }
        activeBoardIndex = 0;
    }

    /**
     * 获取当前激活的对局。
     * @return 当前对局
     */
    public ReversiGame getActiveGame() {
        return boards.get(activeBoardIndex);
    }

    /**
     * 获取当前激活棋盘编号（从 1 开始）。
     * @return 当前棋盘编号
     */
    public int getActiveBoardNumber() {
        return activeBoardIndex + 1;
    }

    /**
     * 获取总棋盘数量。
     * @return 棋盘数量
     */
    public int getBoardCount() {
        return boards.size();
    }

    /**
     * 切换到指定棋盘编号（从 1 开始）。
     * @param boardNumber 目标棋盘编号
     * @return 切换成功返回 true
     */
    public boolean selectBoard(int boardNumber) {
        int index = boardNumber - 1;
        if (index < 0 || index >= boards.size()) {
            return false;
        }
        activeBoardIndex = index;
        return true;
    }
}
