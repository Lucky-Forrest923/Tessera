package com.othello.model;

import com.othello.text.GameTexts;

import java.util.ArrayList;
import java.util.List;

public class MultiBoardManager {
    private final List<BaseGame> games = new ArrayList<>();
    private int currentIndex = 0;

    /**
     * 创建一个多局管理器，并初始化默认的三局游戏。
     *
     * @param size 棋盘大小
     */
    public MultiBoardManager(int size) {
        games.add(new PeaceGame(size));
        games.add(new ReversiGame(size));
        games.add(new Minesweeper(size));
    }

    /**
     * 根据类型新增一局游戏。
     *
     * @param type 游戏类型标识
     * @param size 棋盘大小
     */
    public void add(String type, int size) {
        if (GameTexts.TYPE_PEACE.equalsIgnoreCase(type)) {
            games.add(new PeaceGame(size));
        } else if (GameTexts.TYPE_REVERSI.equalsIgnoreCase(type)) {
            games.add(new ReversiGame(size));
        } else if (GameTexts.TYPE_MINESWEEPER.equalsIgnoreCase(type)) {
            games.add(new Minesweeper(size));
        }
    }

    /**
     * 获取当前正在查看的游戏。
     *
     * @return 当前活动游戏
     */
    public BaseGame getActive() {
        return games.get(currentIndex);
    }

    /**
     * 切换到指定编号的游戏。
     *
     * @param number 目标游戏编号（从 1 开始）
     */
    public void select(int number) {
        if (number > 0 && number <= games.size()) {
            currentIndex = number - 1;
        }
    }

    /**
     * 获取当前所有游戏的只读视图。
     *
     * @return 游戏列表的只读副本
     */
    public List<BaseGame> getGames() {
        return List.copyOf(games);
    }

    /**
     * 获取当前游戏编号。
     *
     * @return 当前游戏编号（从 1 开始）
     */
    public int getCurrentBoardNumber() {
        return currentIndex + 1;
    }
}