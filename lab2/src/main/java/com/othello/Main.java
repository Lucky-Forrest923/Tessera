package com.othello;

import com.othello.controller.GameController;

/**
 * 应用程序入口点。
 */
public class Main {
    /**
     * 启动并管理游戏实例。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try (GameController controller = new GameController()) {
            controller.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}