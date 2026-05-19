package com.othello.controller;

import com.othello.model.BaseGame;
import com.othello.model.GameStatusCode;
import com.othello.model.MultiBoardManager;
import com.othello.model.Position;
import com.othello.view.TerminalUI;

public class GameController {
    private static final int DEFAULT_SIZE = 8;

    private final TerminalUI ui;
    private final MultiBoardManager manager;
    private final StringBuilder inputBuffer = new StringBuilder();
    private String lastMessage = "";
    private boolean lastMessageIsError = false;

    public GameController(TerminalUI ui, MultiBoardManager manager) {
        this.ui = ui;
        this.manager = manager;
    }

    public void run() throws Exception {
        while (true) {
            BaseGame game = manager.getActive();

            // 优先级：错误消息 > pass 提示 > 普通消息
            String msg = lastMessage;
            boolean err = lastMessageIsError;
            if (!err && !game.isOver()
                    && "reversi".equals(game.getType())
                    && game.getLegalMoves().isEmpty()) {
                msg = "当前玩家无合法落子，请按 pass。";
            }

            ui.draw(game, manager.getGames(), manager.getCurrentBoardNumber(),
                    inputBuffer.toString(), msg, err);

            int key = ui.readKey();
            if (key >= 0) {
                inputBuffer.append((char) key);
            } else if (key == -2 && !inputBuffer.isEmpty()) {
                inputBuffer.setLength(inputBuffer.length() - 1);
            } else if (key == -1) {
                if (handleEnter(game)) {
                    break;
                }
            }
        }
    }

    private boolean handleEnter(BaseGame game) {
        String cmd = inputBuffer.toString().trim();
        inputBuffer.setLength(0);
        lastMessage = "";
        lastMessageIsError = false;

        if ("quit".equalsIgnoreCase(cmd)) {
            return true;
        }

        if ("peace".equalsIgnoreCase(cmd) || "reversi".equalsIgnoreCase(cmd)) {
            manager.add(cmd.toLowerCase(), DEFAULT_SIZE);
            lastMessage = "已添加新的 " + cmd + " 游戏";
            return false;
        }

        if ("pass".equalsIgnoreCase(cmd)) {
            try {
                game.pass();
                lastMessage = game.getStatusMessage();
                lastMessageIsError = game.isStatusError();
                // 语义优化：pass 成功时不显示"有效落子"，而是更准确的提示
                if (!lastMessageIsError && game.getCode() == GameStatusCode.MOVE_ACCEPTED) {
                    lastMessage = "Pass 成功！轮到下一位玩家。";
                }
            } catch (UnsupportedOperationException e) {
                lastMessage = "当前游戏不支持 pass";
                lastMessageIsError = true;
            }
            return false;
        }

        Integer num = parseBoardNumber(cmd, manager.getGames().size());
        if (num != null) {
            manager.select(num);
            lastMessage = "已切换到游戏 " + num;
            return false;
        }

        if (isDigits(cmd)) {
            int max = manager.getGames().size();
            lastMessage = "游戏编号超出范围：当前共有 " + max + " 局，请输入 1-" + max + "。";
            lastMessageIsError = true;
            return false;
        }

        Position pos = parsePosition(cmd);
        if (pos != null) {
            game.play(pos);
            lastMessage = game.getStatusMessage();
            lastMessageIsError = game.isStatusError();
            return false;
        }

        if (!cmd.isEmpty()) {
            lastMessage = "输入格式有误！请输入正确的位置格式，如 D4";
            lastMessageIsError = true;
        }
        return false;
    }

    private Integer parseBoardNumber(String input, int max) {
        if (input == null || max < 1) return null;
        String s = input.trim();
        if (!s.matches("\\d+")) return null;
        try {
            int n = Integer.parseInt(s);
            return (n >= 1 && n <= max) ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isDigits(String text) {
        return text != null && !text.isEmpty() && text.matches("\\d+");
    }

    private Position parsePosition(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase();
        if (!s.matches("^[a-z]\\d+$")) return null;
        try {
            int col = s.charAt(0) - 'a';
            int row = Integer.parseInt(s.substring(1)) - 1;
            return new Position(row, col);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}