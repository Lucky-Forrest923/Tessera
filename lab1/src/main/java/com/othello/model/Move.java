package com.othello.model;

/**
 * 移动坐标记录类。
 * 将外部输入的字符串格式转换为系统内部使用的数字索引坐标。
 */
public class Move {
    private final int row, col;

    /**
     * 内部构造函数。
     * @param row 行索引
     * @param col 列索引
     */
    public Move(int row, int col) { this.row = row; this.col = col; }

    /**
     * 从输入字符串解析为坐标对象。
     * @param input 用户输入
     * @return 解析成功返回 Move 对象，格式非法返回 null
     */
    public static Move fromString(String input) {
        if (input == null || input.length() < 2) return null;
        String clean = input.trim().toLowerCase();

        try {
            int col = clean.charAt(0) - 'a';
            int row = Integer.parseInt(clean.substring(1)) - 1;
            return new Move(row, col);
        } catch (Exception e) {
            return null;
        }
    }

    /** @return 行坐标索引 */
    public int row() { return row; }
    /** @return 列坐标索引 */
    public int col() { return col; }
}