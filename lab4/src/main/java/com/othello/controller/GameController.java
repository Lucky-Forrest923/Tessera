package com.othello.controller;

import com.othello.model.BaseGame;
import com.othello.model.GameStatusCode;
import com.othello.model.Minesweeper;
import com.othello.model.MultiBoardManager;
import com.othello.model.OthelloGame;
import com.othello.model.Position;
import com.othello.text.GameTexts;
import com.othello.view.TerminalUI;

public class GameController {
    private static final int DEFAULT_SIZE = 8;

    private final TerminalUI ui;
    private final MultiBoardManager manager;
    private final StringBuilder inputBuffer = new StringBuilder();
    private String lastMessage = "";
    private boolean lastMessageIsError = false;

    /**
     * 创建游戏控制器。
     *
     * @param ui 用户界面
     * @param manager 多局管理器
     */
    public GameController(TerminalUI ui, MultiBoardManager manager) {
        this.ui = ui;
        this.manager = manager;
    }

    /**
     * 运行主循环，持续读取输入并驱动游戏状态更新。
     *
     * @throws Exception 当输入输出或界面刷新失败时抛出
     */
    public void run() throws Exception {
        while (true) {
            BaseGame game = manager.getActive();
            String msg = lastMessage;
            boolean err = lastMessageIsError;
            if (!err && !game.isOver()
                    && GameTexts.isType(game.getType(), GameTexts.TYPE_REVERSI)
                    && game.getLegalMoves().isEmpty()) {
                msg = GameTexts.passHintMessage();
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

    /**
     * 处理一次回车输入。
     *
     * @param game 当前活动游戏
     * @return 如果需要退出程序则返回 {@code true}
     */
    private boolean handleEnter(BaseGame game) {
        String cmd = inputBuffer.toString().trim();
        inputBuffer.setLength(0);
        lastMessage = "";
        lastMessageIsError = false;

        if (GameTexts.COMMAND_QUIT.equalsIgnoreCase(cmd)) {
            return true;
        }

        if (GameTexts.TYPE_PEACE.equalsIgnoreCase(cmd)
                || GameTexts.TYPE_REVERSI.equalsIgnoreCase(cmd)
                || GameTexts.TYPE_MINESWEEPER.equalsIgnoreCase(cmd)) {
            String type = cmd.toLowerCase();
            manager.add(type, DEFAULT_SIZE);
            lastMessage = GameTexts.addedGameMessage(type);
            return false;
        }

        if (GameTexts.COMMAND_PASS.equalsIgnoreCase(cmd)) {
            if (game instanceof OthelloGame) {
                try {
                    ((OthelloGame) game).pass();
                    lastMessage = game.getStatusMessage();
                    lastMessageIsError = game.isStatusError();
                    if (!lastMessageIsError && game.getCode() == GameStatusCode.MOVE_ACCEPTED) {
                        lastMessage = GameTexts.passSuccessMessage();
                    }
                } catch (UnsupportedOperationException e) {
                    lastMessage = GameTexts.passUnsupportedMessage();
                    lastMessageIsError = true;
                }
            } else {
                lastMessage = GameTexts.passUnsupportedMessage();
                lastMessageIsError = true;
            }
            return false;
        }

        String flagPrefix = "(?i)(" + GameTexts.COMMAND_FLAG_SHORT + "|" + GameTexts.COMMAND_FLAG_LONG + ")";
        if (cmd.matches(flagPrefix + "\\s+[a-z]\\d+") || cmd.matches(flagPrefix + "\\s+\\d+[a-z]")) {
            if (game instanceof Minesweeper) {
                String[] parts = cmd.split("\\s+", 2);
                Position pos = parsePosition(parts[1]);
                if (pos != null) {
                    ((Minesweeper) game).flag(pos);
                    lastMessage = game.getStatusMessage();
                    lastMessageIsError = game.isStatusError();
                } else {
                    lastMessage = GameTexts.coordinateFormatErrorMessage();
                    lastMessageIsError = true;
                }
            } else {
                lastMessage = GameTexts.flagUnsupportedMessage();
                lastMessageIsError = true;
            }
            return false;
        }

        Integer num = parseBoardNumber(cmd, manager.getGames().size());
        if (num != null) {
            manager.select(num);
            lastMessage = GameTexts.switchSuccessMessage(num);
            return false;
        }

        if (isDigits(cmd)) {
            int max = manager.getGames().size();
            lastMessage = GameTexts.boardNumberOutOfRangeMessage(max);
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
            lastMessage = GameTexts.inputFormatErrorMessage();
            lastMessageIsError = true;
        }
        return false;
    }

    /**
     * 解析棋盘编号（纯数字或 switch/s + 数字）。
     *
     * @param input 用户输入
     * @param max 当前最大局数
     * @return 合法编号；若解析失败则返回 {@code null}
     */
    private Integer parseBoardNumber(String input, int max) {
        if (input == null || max < 1) return null;
        String s = input.trim();
        int n;
        if (s.matches("\\d+")) {
            n = Integer.parseInt(s);
        } else if (s.matches("(?i)(" + GameTexts.COMMAND_SWITCH_LONG + "|" + GameTexts.COMMAND_SWITCH_SHORT + ")\\s+\\d+")) {
            String[] parts = s.split("\\s+");
            n = Integer.parseInt(parts[1]);
        } else {
            return null;
        }
        return (n >= 1 && n <= max) ? n : null;
    }

    /**
     * 判断字符串是否只包含数字。
     *
     * @param text 待判断文本
     * @return 如果仅由数字组成则返回 {@code true}
     */
    private boolean isDigits(String text) {
        return text != null && !text.isEmpty() && text.matches("\\d+");
    }

    /**
     * 解析坐标：支持 a1 和 1a 两种格式。
     *
     * @param input 用户输入
     * @return 解析成功则返回坐标，否则返回 {@code null}
     */
    private Position parsePosition(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase();
        if (s.matches("^[a-z]\\d+$")) {
            int col = s.charAt(0) - 'a';
            int row = Integer.parseInt(s.substring(1)) - 1;
            return new Position(row, col);
        }
        if (s.matches("^\\d+[a-z]$")) {
            int row = Integer.parseInt(s.replaceAll("[a-z]", "")) - 1;
            int col = s.charAt(s.length() - 1) - 'a';
            return new Position(row, col);
        }
        return null;
    }
}