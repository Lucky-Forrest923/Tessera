package com.othello.controller;

import com.othello.model.Position;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 输入解析类。
 * 负责命令识别、棋盘大小解析与落子坐标解析。
 */
public class InputParser {
    private static final int MIN_SIZE = 4;
    private static final int MAX_SIZE = 16;

    private static final Pattern POSITION_PATTERN = Pattern.compile("^([A-Za-z])(\\d+)$");

    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("^\\d+$");

    /**
     * 判断是否为退出命令。
     * @param command 输入命令
     * @return 是 quit 返回 true
     */
    public boolean isQuitCommand(String command) {
        if (command == null) {
            return false;
        }
        String clean = command.trim();
        return "quit".equalsIgnoreCase(clean);
    }

    /**
     * 解析棋盘大小并校验范围。
     * @param input 用户输入
     * @return 合法棋盘大小，非法返回 null
     */
    public Integer parseBoardSize(String input) {
        if (input == null) {
            return null;
        }

        String clean = input.trim();
        if (clean.isEmpty()) {
            return null;
        }

        try {
            int size = Integer.parseInt(clean);
            if (size < MIN_SIZE || size > MAX_SIZE || size % 2 != 0) {
                return null;
            }
            return size;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 解析落子坐标，例如 A1、d3、Z26、A12。
     * 支持任意长度的行号数字。
     * @param input 用户输入
     * @return 解析成功返回 Position，失败返回 null
     */
    public Position parsePosition(String input) {
        if (input == null) {
            return null;
        }

        String clean = input.trim().toLowerCase();
        Matcher matcher = POSITION_PATTERN.matcher(clean);

        if (!matcher.matches()) {
            return null;
        }

        String colStr = matcher.group(1);
        String rowStr = matcher.group(2);

        try {
            int col = colStr.charAt(0) - 'a';
            int row = Integer.parseInt(rowStr) - 1;

            return new Position(row, col);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 解析棋盘编号输入。
     * @param input 用户输入
     * @param maxBoardNumber 最大棋盘编号
     * @return 合法棋盘编号，非法返回 null
     */
    public Integer parseBoardNumber(String input, int maxBoardNumber) {
        if (input == null || maxBoardNumber < 1) {
            return null;
        }

        String clean = input.trim();
        if (clean.isEmpty()) {
            return null;
        }

        // 使用正则验证纯数字
        if (!DIGITS_ONLY_PATTERN.matcher(clean).matches()) {
            return null;
        }

        try {
            int boardNumber = Integer.parseInt(clean);
            if (boardNumber < 1 || boardNumber > maxBoardNumber) {
                return null;
            }
            return boardNumber;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

}