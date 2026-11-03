package com.gameplatform.games.minesweeper;

import com.gameplatform.model.BaseGame;
import com.gameplatform.model.Position;
import com.gameplatform.renderer.GamePanelRenderer;

import java.util.ArrayList;
import java.util.List;

public class Minesweeper extends BaseGame {
    private static final int TOTAL_MINES = 10;
    private final MineField field;
    private int openedCount = 0;
    private int flagCount = 0;
    private boolean won = false;

    public Minesweeper(int size) {
        super(size);
        field = new MineField(size);
    }

    @Override
    public String getType() {
        return "minesweeper";
    }

    @Override
    public String getDisplayName() {
        return "扫雷";
    }

    @Override
    public boolean supportsFlag() { return true; }

    @Override
    public GamePanelRenderer createRenderer() {
        return new MinesweeperRenderer();
    }

    @Override
    public boolean processCommand(String cmd) {
        if (isOver()) {
            setStatus(MinesweeperTexts.GAME_OVER, true);
            return true;
        }

        if (cmd.matches("(?i)^(f|flag)\\s+[a-z0-9]+$")) {
            String posStr = cmd.split("\\s+", 2)[1];
            Position pos = parsePosition(posStr);
            if (pos != null) {
                flag(pos);
            } else {
                setStatus(MinesweeperTexts.INVALID_COORD, true);
            }
            return true;
        }

        Position pos = parsePosition(cmd);
        if (pos != null) {
            play(pos);
            return true;
        }

        return false;
    }

    private void play(Position position) {
        int row = position.row();
        int col = position.col();
        if (field.isOutOfBounds(row, col)) {
            setStatus(MinesweeperTexts.OUT_OF_BOUNDS, true);
            return;
        }
        CellStatus current = field.getCell(row, col);
        if (current == CellStatus.FLAGGED) {
            setStatus(MinesweeperTexts.CANNOT_OPEN_FLAGGED, true);
            return;
        }
        if (current != CellStatus.COVERED) {
            setStatus(MinesweeperTexts.ALREADY_OPENED, true);
            return;
        }

        if (!field.isMinesGenerated()) {
            field.generateMines(position, TOTAL_MINES);
        }
        openCell(row, col);
    }

    private void flag(Position position) {
        int row = position.row();
        int col = position.col();
        if (field.isOutOfBounds(row, col)) {
            setStatus(MinesweeperTexts.OUT_OF_BOUNDS, true);
            return;
        }
        CellStatus current = field.getCell(row, col);
        if (current == CellStatus.FLAGGED) {
            field.toggleFlag(row, col);
            flagCount--;
            setStatus(MinesweeperTexts.UNFLAG_SUCCESS, false);
        } else if (current == CellStatus.COVERED) {
            field.toggleFlag(row, col);
            flagCount++;
            setStatus(MinesweeperTexts.FLAG_SUCCESS, false);
        } else {
            setStatus(MinesweeperTexts.ALREADY_OPENED, true);
        }
    }

    private void openCell(int row, int col) {
        CellStatus opened = field.openCell(row, col);
        if (opened == CellStatus.MINE) {
            won = false;
            setOver();
            setStatus(MinesweeperTexts.HIT_MINE, true);
            return;
        }
        openedCount++;
        if (openedCount == size * size - TOTAL_MINES) {
            won = true;
            setOver();
            setStatus(MinesweeperTexts.ALL_SAFE_OPENED, false);
        } else {
            setStatus(MinesweeperTexts.OPEN_SUCCESS, false);
        }
    }

    public void play(int row, int col) {
        play(new Position(row, col));
    }

    public void flag(int row, int col) {
        flag(new Position(row, col));
    }

    @Override
    public void onCellClick(int row, int col, boolean isSecondary, boolean isFlagMode) {
        if (isSecondary || isFlagMode) {
            flag(row, col);
        } else {
            play(row, col);
        }
    }

    public CellStatus getCellState(int row, int col) {
        return field.getCell(row, col);
    }

    @Override
    public void prepareDemo() {
        field.generateFixedMines(List.of(
                new Position(0, 0), new Position(0, 3), new Position(0, 5),
                new Position(1, 6), new Position(2, 0), new Position(3, 3),
                new Position(4, 6), new Position(5, 2), new Position(6, 5),
                new Position(7, 0)
        ));
    }

    @Override
    public List<DemoStep> getDemoSteps() {
        return List.of(
                new DemoStep("b2", "首开b2——系统保证第一次翻开不为雷"),
                new DemoStep("b3", "翻开b3——显示相邻雷数"),
                new DemoStep("flag c3", "在c3插旗——标记疑似雷格"),
                new DemoStep("flag c3", "再次插旗取消——旗子开关演示"),
                new DemoStep("c2", "翻开c2——数字提示进一步缩小范围"),
                new DemoStep("d2", "翻开d2——展示推理过程")
        );
    }

    @Override
    public int getDemoStepDelayMs() {
        return 1000;
    }

    @Override
    public List<String> getExtraStatusLines() {
        List<String> lines = new ArrayList<>();
        lines.add(MinesweeperTexts.minesAndFlagsLine(TOTAL_MINES, flagCount));
        if (isOver()) {
            lines.add(won ? MinesweeperTexts.RESULT_WIN : MinesweeperTexts.RESULT_LOSE);
        } else {
            lines.add(MinesweeperTexts.FIRST_OPEN_SAFE);
        }
        return lines;
    }

    @Override
    public List<String> getAvailableCommands() {
        return List.of(MinesweeperTexts.COMMAND_OPEN, MinesweeperTexts.COMMAND_FLAG, MinesweeperTexts.COMMAND_NOTE);
    }


    @Override
    public String getCellChar(int row, int col) {
        CellStatus cell = field.getCell(row, col);
        return switch (cell) {
            case COVERED -> "#";
            case FLAGGED -> "F";
            case MINE -> "*";
            case OPENED_0 -> ".";
            case OPENED_1 -> "1";
            case OPENED_2 -> "2";
            case OPENED_3 -> "3";
            case OPENED_4 -> "4";
            case OPENED_5 -> "5";
            case OPENED_6 -> "6";
            case OPENED_7 -> "7";
            case OPENED_8 -> "8";
        };
    }
}