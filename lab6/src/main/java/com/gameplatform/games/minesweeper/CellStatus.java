package com.gameplatform.games.minesweeper;

public enum CellStatus {
    COVERED, FLAGGED, MINE,
    OPENED_0, OPENED_1, OPENED_2, OPENED_3, OPENED_4, OPENED_5, OPENED_6, OPENED_7, OPENED_8;

    public static CellStatus forNumber(int n) {
        return switch (n) {
            case 0 -> OPENED_0; case 1 -> OPENED_1; case 2 -> OPENED_2;
            case 3 -> OPENED_3; case 4 -> OPENED_4; case 5 -> OPENED_5;
            case 6 -> OPENED_6; case 7 -> OPENED_7; case 8 -> OPENED_8;
            default -> throw new IllegalArgumentException("无效的数字：" + n);
        };
    }
}
