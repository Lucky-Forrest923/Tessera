package com.othello.model;

import com.othello.text.GameTexts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Minesweeper extends BaseGame {
    private static final int TOTAL_MINES = 10;
    private final CellStatus[][] grid;      // 只存翻开状态
    private final boolean[][] mines;        // true 表示该格有雷
    private boolean firstOpen = true;
    private int openedCount = 0;
    private int flagCount = 0;

    /**
     * 创建一局扫雷游戏，并初始化棋盘状态。
     *
     * @param size 棋盘大小
     */
    public Minesweeper(int size) {
        super(size);
        grid = new CellStatus[size][size];
        mines = new boolean[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = CellStatus.COVERED;
            }
        }
    }

    /**
     * 返回游戏类型标识。
     *
     * @return 游戏类型标识
     */
    @Override
    public String getType() {
        return GameTexts.TYPE_MINESWEEPER;
    }

    /**
     * 返回当前扫雷状态消息。
     *
     * @return 状态消息文本
     */
    @Override
    public String getStatusMessage() {
        return GameTexts.minesweeperStatusMessage(this.code);
    }

    /**
     * 翻开指定位置的格子。
     *
     * @param position 目标位置
     */
    @Override
    public void play(Position position) {
        if (position == null) {
            setCode(GameStatusCode.INVALID_COORDINATE);
            return;
        }
        int row = position.row();
        int col = position.col();
        if (isOutOfBounds(row, col)) {
            setCode(GameStatusCode.OUT_OF_BOUNDS);
            return;
        }
        if (isOver()) {
            setCode(GameStatusCode.GAME_ALREADY_OVER);
            return;
        }
        CellStatus current = grid[row][col];
        if (current == CellStatus.FLAGGED) {
            setCode(GameStatusCode.CANNOT_OPEN_FLAGGED);
            return;
        }
        if (current != CellStatus.COVERED) {
            setCode(GameStatusCode.OCCUPIED);
            return;
        }

        if (firstOpen) {
            generateMines(position);
            firstOpen = false;
        }
        openCell(row, col);
    }

    /**
     * 对指定位置进行插旗或取消插旗。
     *
     * @param position 目标位置
     */
    public void flag(Position position) {
        if (position == null) {
            setCode(GameStatusCode.INVALID_COORDINATE);
            return;
        }
        int row = position.row();
        int col = position.col();
        if (isOutOfBounds(row, col)) {
            setCode(GameStatusCode.OUT_OF_BOUNDS);
            return;
        }
        if (isOver()) {
            setCode(GameStatusCode.GAME_ALREADY_OVER);
            return;
        }
        CellStatus current = grid[row][col];
        if (current == CellStatus.FLAGGED) {
            grid[row][col] = CellStatus.COVERED;
            flagCount--;
            setCode(GameStatusCode.UNFLAG_SUCCESS);
        } else if (current == CellStatus.COVERED) {
            grid[row][col] = CellStatus.FLAGGED;
            flagCount++;
            setCode(GameStatusCode.FLAG_SUCCESS);
        } else {
            setCode(GameStatusCode.OCCUPIED);
        }
    }

    /**
     * 获取当前所有仍可操作的格子。
     *
     * @return 未翻开格子列表
     */
    @Override
    public List<Position> getLegalMoves() {
        List<Position> list = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] == CellStatus.COVERED) {
                    list.add(new Position(r, c));
                }
            }
        }
        return list;
    }

    /**
     * 返回指定格子的显示字符。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 格子显示字符
     */
    @Override
    public String getCellChar(int row, int col) {
        CellStatus cell = grid[row][col];
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

    /**
     * 返回扫雷的附加状态信息。
     *
     * @return 状态栏文本
     */
    @Override
    public String getGameSpecificStatus() {
        return GameTexts.minesweeperGameStatus(TOTAL_MINES, flagCount);
    }

    /**
     * 生成地雷，并保证首开位置不放雷。
     *
     * @param safe 首次翻开的安全位置
     */
    private void generateMines(Position safe) {
        Random rand = new Random();
        int minesPlaced = 0;
        while (minesPlaced < TOTAL_MINES) {
            int r = rand.nextInt(size);
            int c = rand.nextInt(size);
            if (r == safe.row() && c == safe.col()) continue;
            if (mines[r][c]) continue;
            mines[r][c] = true;
            minesPlaced++;
        }
    }

    /**
     * 翻开指定格子，并根据结果更新状态。
     *
     * @param row 行坐标
     * @param col 列坐标
     */
    private void openCell(int row, int col) {
        if (mines[row][col]) {
            grid[row][col] = CellStatus.MINE;   // 翻开雷，此时才设为 MINE
            setOver();
            setCode(GameStatusCode.MINE_HIT);
            return;
        }
        int mineCount = countAdjacentMines(row, col);
        grid[row][col] = CellStatus.forNumber(mineCount);
        openedCount++;
        if (openedCount == size * size - TOTAL_MINES) {
            setOver();
            setCode(GameStatusCode.GAME_OVER_ALL_SAFE_OPENED);
        } else {
            setCode(GameStatusCode.MOVE_ACCEPTED);
        }
    }

    /**
     * 统计某个位置周围 8 格中的地雷数量。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 相邻地雷数量
     */
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr;
                int nc = col + dc;
                if (!isOutOfBounds(nr, nc) && mines[nr][nc]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 判断坐标是否越界。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 如果越界则返回 {@code true}
     */
    private boolean isOutOfBounds(int row, int col) {
        return row < 0 || row >= size || col < 0 || col >= size;
    }
}