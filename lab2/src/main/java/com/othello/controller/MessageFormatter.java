package com.othello.controller;

import com.othello.model.GameState;
import com.othello.model.GameStatusCode;
import com.othello.model.Piece;

/**
 * 消息格式化器。
 * 负责将游戏状态转换为用户友好的提示消息。
 * 现在主要处理需要动态参数的消息（如终局统计）。
 */
public class MessageFormatter {

    /**
     * 根据游戏状态格式化状态消息。
     * @param state 游戏状态
     * @param blackCount 黑棋数量（用于终局消息）
     * @param whiteCount 白棋数量（用于终局消息）
     * @return 格式化后的消息
     */
    public String formatStatusMessage(GameState state, int blackCount, int whiteCount) {
        GameStatusCode code = state.getStatusCode();

        // 对于终局消息，需要额外格式化胜负信息
        if (code == GameStatusCode.GAME_OVER_FULL || code == GameStatusCode.GAME_OVER_NO_MOVES) {
            return formatGameOverMessage(code, blackCount, whiteCount);
        }

        // 其他消息直接使用枚举自带模板
        return code.getMessageTemplate();
    }

    /**
     * 格式化游戏结束消息（需要动态参数）。
     */
    private String formatGameOverMessage(GameStatusCode code, int blackCount, int whiteCount) {
        String result;
        if (blackCount > whiteCount) {
            result = "黑方胜利！";
        } else if (whiteCount > blackCount) {
            result = "白方胜利！";
        } else {
            result = "平局！";
        }

        String baseMessage = code.getMessageTemplate();
        return String.format("%s 黑: %d  白: %d。%s", baseMessage, blackCount, whiteCount, result);
    }

    /**
     * 获取玩家名称。
     * @param piece 棋子类型
     * @return 玩家名称
     */
    public String getPlayerName(Piece piece) {
        if (piece == Piece.BLACK) {
            return "黑方";
        }
        if (piece == Piece.WHITE) {
            return "白方";
        }
        return "无";
    }
}