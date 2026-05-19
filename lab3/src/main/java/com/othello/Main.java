package com.othello;

import com.othello.controller.GameController;
import com.othello.model.MultiBoardManager;
import com.othello.view.TerminalUI;

public class Main {
    public static void main(String[] args) {
        try (TerminalUI ui = new TerminalUI()) {
            MultiBoardManager manager = new MultiBoardManager(8);
            new GameController(ui, manager).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}