package com.gameplatform.controller;

import com.gameplatform.model.BaseGame;
import com.gameplatform.model.MultiBoardManager;
import com.gameplatform.text.GameTexts;
import com.gameplatform.view.TerminalUI;

import java.util.List;

public class GameController {
    private static final int DEFAULT_SIZE = 8;
    private static final int DEMO_GAME_SWITCH_DELAY_MS = 1500; // 切换游戏提示停留时间
    private final TerminalUI ui;
    private final MultiBoardManager manager;
    private final StringBuilder inputBuffer = new StringBuilder();
    private String platformMessage = "";
    private boolean platformMessageIsError = false;

    // Demo 模式状态
    private boolean demoMode = false;
    private int demoGameIndex = 0;
    private int demoStepIndex = 0; // -1 表示尚未开始当前游戏的演示
    private long demoNextStepAt = 0L;

    public GameController(TerminalUI ui, MultiBoardManager manager) {
        this.ui = ui;
        this.manager = manager;
    }

    public void run() throws Exception {
        while (true) {
            BaseGame game = manager.getActive();

            String displayMsg = platformMessage.isEmpty() ? game.getStatusMessage() : platformMessage;
            boolean displayErr = platformMessage.isEmpty() ? game.isStatusError() : platformMessageIsError;
            if (demoMode) {
                String prefix = platformMessage.isEmpty() ? game.getStatusMessage() : platformMessage;
                displayMsg = prefix + "（" + GameTexts.DEMO_ABORT_HINT + "）";
                displayErr = platformMessageIsError;
            }

            ui.draw(game, manager.getGames(), manager.getCurrentBoardNumber(),
                    inputBuffer.toString(), displayMsg, displayErr, manager.getAddableTypes());

            if (demoMode) {
                int key = ui.pollKey();
                if (key != -3) {
                    abortDemo();
                    continue;
                }
                if (System.currentTimeMillis() >= demoNextStepAt) {
                    boolean finished = tickDemo();
                    if (finished) {
                        demoMode = false;
                        platformMessage = GameTexts.DEMO_FINISHED;
                        platformMessageIsError = false;
                        manager.resetToDefaults(DEFAULT_SIZE);
                        demoGameIndex = 0;
                        demoStepIndex = 0;
                    }
                }
                continue;
            }

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
        platformMessage = "";
        platformMessageIsError = false;

        if (GameTexts.COMMAND_QUIT.equalsIgnoreCase(cmd)) {
            return true;
        }
        if (GameTexts.COMMAND_DEMO.equalsIgnoreCase(cmd)) {
            startDemo();
            return false;
        }
        if (manager.isSupportedType(cmd)) {
            String type = cmd.toLowerCase();
            manager.add(type, DEFAULT_SIZE);
            platformMessage = "已添加新的 " + type + " 游戏";
            return false;
        }
        Integer num = parseBoardNumber(cmd, manager.getGames().size());
        if (num != null) {
            manager.select(num);
            platformMessage = "已切换到第 " + num + " 局";
            return false;
        }
        if (isDigits(cmd)) {
            int max = manager.getGames().size();
            platformMessage = "游戏编号超出范围：当前共有 " + max + " 局，请输入 1-" + max + "。";
            platformMessageIsError = true;
            return false;
        }
        if (game.processCommand(cmd)) {
            return false;
        }
        if (!cmd.isEmpty()) {
            platformMessage = "未知命令或输入格式错误！请参考右侧可用命令列表。";
            platformMessageIsError = true;
        }
        return false;
    }

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

    private boolean isDigits(String text) {
        return text != null && !text.isEmpty() && text.matches("\\d+");
    }

    private void startDemo() {
        if (manager.getGames().isEmpty()) {
            platformMessage = "没有可演示的游戏";
            platformMessageIsError = true;
            return;
        }
        demoMode = true;
        demoGameIndex = 0;
        demoStepIndex = -1; // 标记尚未开始当前游戏演示
        manager.select(1);
        // 显示第一个游戏的开始提示
        platformMessage = "即将开始演示：「" + manager.getActive().getDisplayName() + "」";
        platformMessageIsError = false;
        demoNextStepAt = System.currentTimeMillis() + DEMO_GAME_SWITCH_DELAY_MS;
        inputBuffer.setLength(0);
    }

    private boolean tickDemo() {
        List<BaseGame> games = manager.getGames();
        if (demoGameIndex >= games.size()) {
            return true;
        }

        BaseGame currentGame = games.get(demoGameIndex);
        List<BaseGame.DemoStep> steps = currentGame.getDemoSteps();

        // 处理尚未开始当前游戏演示的状态
        if (demoStepIndex == -1) {
            if (steps.isEmpty()) {
                // 无演示步骤，跳过
                demoGameIndex++;
                if (demoGameIndex >= games.size()) {
                    return true;
                }
                manager.select(demoGameIndex + 1);
                BaseGame nextGame = games.get(demoGameIndex);
                platformMessage = "游戏「" + currentGame.getDisplayName() + "」无演示步骤，已跳过。切换到「" + nextGame.getDisplayName() + "」";
                platformMessageIsError = false;
                demoStepIndex = -1;
            } else {
                platformMessage = "开始演示：「" + currentGame.getDisplayName() + "」";
                platformMessageIsError = false;
                demoStepIndex = 0;
            }
            demoNextStepAt = System.currentTimeMillis() + DEMO_GAME_SWITCH_DELAY_MS;
            return false;
        }

        // 当前游戏演示完毕，切换到下一个
        if (demoStepIndex >= steps.size()) {
            demoGameIndex++;
            demoStepIndex = -1;
            if (demoGameIndex >= games.size()) {
                return true;
            }
            manager.select(demoGameIndex + 1);
            BaseGame nextGame = games.get(demoGameIndex);
            platformMessage = "演示完成：「" + currentGame.getDisplayName() + "」。切换到下一个游戏：「" + nextGame.getDisplayName() + "」";
            platformMessageIsError = false;
            demoNextStepAt = System.currentTimeMillis() + DEMO_GAME_SWITCH_DELAY_MS;
            return false;
        }

        // 正常执行步骤
        BaseGame.DemoStep step = steps.get(demoStepIndex);
        platformMessage = step.explanation();
        platformMessageIsError = false;
        currentGame.processCommand(step.command());
        demoStepIndex++;

        int delay = currentGame.getDemoStepDelayMs();
        demoNextStepAt = System.currentTimeMillis() + delay;
        return false;
    }

    private void abortDemo() {
        demoMode = false;
        demoGameIndex = 0;
        demoStepIndex = 0;
        manager.resetToDefaults(DEFAULT_SIZE);
        platformMessage = GameTexts.DEMO_ABORTED;
        platformMessageIsError = false;
        inputBuffer.setLength(0);
    }
}