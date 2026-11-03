package com.gameplatform.games.chess;

import com.gameplatform.model.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChessGameTest {

    /**
     * 复现用户报告的场景：
     * 黑王在d1=(0,3)，白马在f2=(1,5)，黑车在h2=(1,7)
     * 黑方行棋，车吃马（R: (1,7)→(1,5)）
     * 验证是否被错误地拒绝。
     */
    @Test
    public void testBlackRookCapturesWhiteKnight() {
        ChessBoard board = new ChessBoard(8);
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board.set(r, c, ChessPiece.EMPTY);

        board.set(0, 3, ChessPiece.BKING);   // 黑王 d1
        board.set(1, 7, ChessPiece.BROOK);   // 黑车 h2
        board.set(1, 5, ChessPiece.WKNIGHT); // 白马 f2
        board.set(7, 4, ChessPiece.WKING);   // 白王 e8（默认位置）

        // 为黑车(1,7)生成走法
        var candidates = ChessMoveValidator.generateCandidates(
            board, 8, null,
            false, false, false, false, false, false,
            new Position(1, 7));

        // 找车 f2→h2...(1,7)→(1,5)的吃子走法
        ChessMoveCandidate capture = null;
        for (var c : candidates) {
            if (c.to.row() == 1 && c.to.col() == 5) {
                capture = c;
                break;
            }
        }
        assertNotNull(capture, "黑车(1,7)→(1,5)吃马应该生成候选走法");

        // 检查吃马后黑王是否安全
        boolean safe = ChessMoveValidator.isKingSafeAfterMove(board, 8, capture, null);
        assertTrue(safe, "黑车吃白马后，黑王应该安全（没有白子攻击(0,3)）");
    }

    /**
     * 检查白马f2=(1,5)是否确实将军黑王d1=(0,3)
     */
    @Test
    public void testKnightChecksBlackKing() {
        ChessBoard board = new ChessBoard(8);
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board.set(r, c, ChessPiece.EMPTY);

        board.set(0, 3, ChessPiece.BKING);   // 黑王 d1
        board.set(1, 5, ChessPiece.WKNIGHT); // 白马 f2

        // 白马在f2=(1,5)是否攻击d1=(0,3)？
        boolean attacked = ChessMoveValidator.isSquareAttacked(
            ChessMoveValidator.boardState(board, 8), 0, 3, true, 8);
        assertTrue(attacked, "白马f2应该将军黑王d1（马的日字走法）");
    }

    /**
     * 验证吃马后，白子不能再攻击(0,3)
     */
    @Test
    public void testAfterCaptureNoWhitePieceAttacksBlackKing() {
        ChessBoard board = new ChessBoard(8);
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board.set(r, c, ChessPiece.EMPTY);

        board.set(0, 3, ChessPiece.BKING);   // 黑王
        board.set(7, 4, ChessPiece.WKING);   // 白王（很远）
        // 没有其他白子

        boolean attacked = ChessMoveValidator.isSquareAttacked(
            ChessMoveValidator.boardState(board, 8), 0, 3, true, 8);
        assertFalse(attacked, "没有白子能攻击黑王(0,3)");
    }

    /**
     * 演示：如果棋盘上还有一个白兵在e2=(1,4)，它会攻击黑王d1=(0,3)。
     * 即使你吃了白马，黑王仍然被白兵将军，所以吃马走法被正确拒绝。
     * 这种情况下提示"王会被将军"是正确的。
     */
    @Test
    public void testPawnDiscoveredCheckExample() {
        ChessBoard board = new ChessBoard(8);
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board.set(r, c, ChessPiece.EMPTY);

        board.set(0, 3, ChessPiece.BKING);    // 黑王 d1
        board.set(1, 5, ChessPiece.WKNIGHT);  // 白马 f2
        board.set(1, 7, ChessPiece.BROOK);    // 黑车 h2
        board.set(1, 4, ChessPiece.WPAWN);    // 白兵 e2 — 攻击(0,3)!
        board.set(7, 4, ChessPiece.WKING);    // 白王 e8

        // 验证白兵确实攻击(0,3)
        boolean pawnAttacks = ChessMoveValidator.isSquareAttacked(
            ChessMoveValidator.boardState(board, 8), 0, 3, true, 8);
        assertTrue(pawnAttacks, "白兵(1,4)应该攻击黑王(0,3)");

        // 生成黑车走法
        var candidates = ChessMoveValidator.generateCandidates(
            board, 8, null,
            false, false, false, false, false, false,
            new Position(1, 7));

        ChessMoveCandidate capture = null;
        for (var c : candidates) {
            if (c.to.row() == 1 && c.to.col() == 5) {
                capture = c;
                break;
            }
        }
        assertNotNull(capture, "黑车吃马走法应生成");

        // 验证：白兵仍在将军，所以吃马后黑王不安全
        boolean safe = ChessMoveValidator.isKingSafeAfterMove(board, 8, capture, null);
        assertFalse(safe, "有白兵攻击(0,3)时，吃马后黑王仍不安全");
    }
}
