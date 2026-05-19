package com.othello;

import com.othello.controller.GameController;
import com.othello.model.MultiBoardManager;
import com.othello.view.TerminalUI;

public class Main {
    /**
     * 程序入口，创建界面和控制器并启动游戏循环。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try (TerminalUI ui = new TerminalUI()) {
            MultiBoardManager manager = new MultiBoardManager(8);
            new GameController(ui, manager).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}