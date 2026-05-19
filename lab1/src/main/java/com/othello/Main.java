package com.othello;

import com.othello.logic.Game;

/**
 * 应用程序入口点。
 */
public class Main {
    /**
     * 启动并管理游戏实例。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try (Game game = new Game()) {
            game.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}