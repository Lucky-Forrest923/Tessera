package com.gameplatform.games.minesweeper;

import com.gameplatform.model.Position;

import java.util.List;
import java.util.Random;

public class MineField {
    private final int size;
    private final CellStatus[][] grid;
    private final boolean[][] mines;
    private boolean minesGenerated = false;

    public MineField(int size) {
        this.size = size;
        this.grid = new CellStatus[size][size];
        this.mines = new boolean[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = CellStatus.COVERED;
            }
        }
    }

    public int getSize() {
        return size;
    }

    public boolean isOutOfBounds(int row, int col) {
        return row < 0 || row >= size || col < 0 || col >= size;
    }

    public boolean isMinesGenerated() {
        return minesGenerated;
    }

    public CellStatus getCell(int row, int col) {
        return grid[row][col];
    }

    public void generateMines(Position safe, int totalMines) {
        if (minesGenerated) return;
        Random rand = new Random();
        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int r = rand.nextInt(size);
            int c = rand.nextInt(size);
            if (r == safe.row() && c == safe.col()) continue;
            if (mines[r][c]) continue;
            mines[r][c] = true;
            minesPlaced++;
        }
        minesGenerated = true;
    }

    /**
     * 演示模式专用：按固定坐标布雷，避免随机踩雷导致演示失败。
     */
    public void generateFixedMines(List<Position> minePositions) {
        if (minesGenerated) return;
        for (Position p : minePositions) {
            if (!isOutOfBounds(p.row(), p.col())) {
                mines[p.row()][p.col()] = true;
            }
        }
        minesGenerated = true;
    }

    public void toggleFlag(int row, int col) {
        CellStatus current = grid[row][col];
        if (current == CellStatus.FLAGGED) {
            grid[row][col] = CellStatus.COVERED;
            return;
        }
        if (current == CellStatus.COVERED) {
            grid[row][col] = CellStatus.FLAGGED;
        }
    }

    public CellStatus openCell(int row, int col) {
        if (mines[row][col]) {
            grid[row][col] = CellStatus.MINE;
            return CellStatus.MINE;
        }
        int mineCount = countAdjacentMines(row, col);
        CellStatus status = CellStatus.forNumber(mineCount);
        grid[row][col] = status;
        return status;
    }

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

    public void reset() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = CellStatus.COVERED;
                mines[r][c] = false;
            }
        }
        minesGenerated = false;
    }
}
