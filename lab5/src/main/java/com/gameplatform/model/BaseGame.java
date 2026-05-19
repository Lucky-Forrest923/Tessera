package com.gameplatform.model;

import java.util.List;

public abstract class BaseGame {
    protected final int size;
    protected boolean over = false;
    protected String statusMessage = "游戏开始！";
    protected boolean statusIsError = false;

    public BaseGame(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean isOver() {
        return over;
    }

    protected void setOver() {
        this.over = true;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean isStatusError() {
        return statusIsError;
    }

    protected void setStatus(String msg, boolean isError) {
        this.statusMessage = msg;
        this.statusIsError = isError;
    }

    /**
     * @param cmd 用户输入的命令
     * @return 如果游戏认识并处理了该指令，返回 true
     */
    public abstract boolean processCommand(String cmd);

    /**
     * 返回在UI状态面板中额外显示的游戏专属信息（如比分、剩余雷数）
     */
    public abstract List<String> getExtraStatusLines();

    /**
     * 返回该游戏专属的控制台可用命令提示文本
     */
    public abstract List<String> getAvailableCommands();

    public abstract String getType();

    public abstract String getDisplayName();

    public abstract String getCellChar(int row, int col);

    public abstract List<Position> getLegalMoves();

    /**
     * 返回本游戏的 Demo 自动演示步骤（命令 + 讲解）。
     * 每条命令必须是可被 processCommand() 直接消费的合法输入。
     */
    public List<DemoStep> getDemoSteps() {
        return List.of();
    }

    /**
     * Demo 单步之间的停顿时长（毫秒）。
     */
    public int getDemoStepDelayMs() {
        return 1200;
    }

    /**
     * 是否在 UI 上用 + 号高亮显示当前所有合法落子点
     */
    public boolean showLegalMovesAsHints() {
        return false;
    }

    /**
     * 辅助工具方法：解析 a1 或 1a 格式的坐标
     */
    protected Position parsePosition(String input) {
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

    /**
     * 演示步骤数据类
     */
    public record DemoStep(String command, String explanation) {}
}