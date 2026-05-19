package com.othello.model;

import com.othello.text.GameTexts;

public enum CellStatus {
    COVERED,       // 未翻开
    FLAGGED,       // 插旗
    MINE,          // 雷（仅在踩雷后显示）
    OPENED_0,
    OPENED_1,
    OPENED_2,
    OPENED_3,
    OPENED_4,
    OPENED_5,
    OPENED_6,
    OPENED_7,
    OPENED_8;

    /**
     * 根据数字返回对应的扫雷打开状态。
     *
     * @param n 相邻地雷数量
     * @return 对应的打开状态
     * @throws IllegalArgumentException 当数字不在 0~8 范围内时抛出
     */
    public static CellStatus forNumber(int n) {
        return switch (n) {
            case 0 -> OPENED_0;
            case 1 -> OPENED_1;
            case 2 -> OPENED_2;
            case 3 -> OPENED_3;
            case 4 -> OPENED_4;
            case 5 -> OPENED_5;
            case 6 -> OPENED_6;
            case 7 -> OPENED_7;
            case 8 -> OPENED_8;
            default -> throw new IllegalArgumentException(GameTexts.invalidCellNumberMessage(n));
        };
    }
}
