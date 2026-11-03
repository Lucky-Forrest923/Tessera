package com.gameplatform.games.chess;

import com.gameplatform.model.BaseGame;
import com.gameplatform.model.Position;
import com.gameplatform.model.TurnBasedGame;
import com.gameplatform.renderer.GamePanelRenderer;

import java.util.ArrayList;
import java.util.List;

public class ChessGame extends BaseGame implements TurnBasedGame {
    private final ChessBoard board;
    private boolean isWhiteTurn = true;

    private ChessMove lastMove = null;

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteLeftRookMoved = false;
    private boolean whiteRightRookMoved = false;
    private boolean blackLeftRookMoved = false;
    private boolean blackRightRookMoved = false;

    private int whiteKingRow = 7, whiteKingCol = 4;
    private int blackKingRow = 0, blackKingCol = 4;

    public ChessGame(int size) {
        super(size);
        this.board = new ChessBoard(size);
        initializeBoard();
    }

    private void initializeBoard() {
        board.initializeStandardSetup();
    }

    @Override
    public String getType() {
        return "chess";
    }

    @Override
    public String getDisplayName() {
        return "国际象棋";
    }

    @Override
    public GamePanelRenderer createRenderer() {
        return new ChessRenderer();
    }

    @Override
    public boolean processCommand(String cmd) {
        if (isOver()) {
            setStatus(ChessTexts.GAME_OVER, true);
            return true;
        }

        if (cmd.matches("(?i)^(m|move)\\s+[a-z0-9]+\\s+[a-z0-9]+(\\s+[qrbn])?$")) {
            String[] parts = cmd.split("\\s+");
            Position from = parsePosition(parts[1]);
            Position to = parsePosition(parts[2]);
            boolean promoSpecified = parts.length > 3;
            char promo = promoSpecified ? parts[3].charAt(0) : 'q';

            if (from != null && to != null) {
                playWithPromotion(from, to, promo, promoSpecified);
            } else {
                setStatus(ChessTexts.INVALID_COORD, true);
            }
            return true;
        }
        return false;
    }

    private void playWithPromotion(Position from, Position to, char promoteTo, boolean promoSpecified) {
        int fr = from.row();
        int fc = from.col();
        int tr = to.row();
        int tc = to.col();

        if (isOutOfBounds(fr, fc) || isOutOfBounds(tr, tc)) {
            setStatus(ChessTexts.OUT_OF_BOUNDS, true);
            return;
        }

        ChessPiece piece = board.get(fr, fc);
        if (piece == null || piece.isEmpty()) {
            setStatus(ChessTexts.NO_PIECE, true);
            return;
        }
        if (piece.isWhite() != isWhiteTurn) {
            setStatus(ChessTexts.NOT_YOUR_TURN, true);
            return;
        }

        List<ChessMoveCandidate> candidates = generateCandidates(from);
        ChessMoveCandidate selected = null;
        for (ChessMoveCandidate mc : candidates) {
            if (mc.to.row() == tr && mc.to.col() == tc) {
                selected = mc;
                break;
            }
        }

        if (selected == null) {
            setStatus(ChessTexts.ILLEGAL_MOVE, true);
            return;
        }

        if (promoSpecified && !selected.promotion) {
            setStatus(ChessTexts.PROMOTION_NOT_ALLOWED, true);
            return;
        }

        if (selected.promotion) {
            ChessPiece promotionPiece = getPromotionPiece(promoteTo, piece.isWhite());
            if (promotionPiece == null) {
                setStatus(ChessTexts.INVALID_PROMOTION, true);
                return;
            }
            selected.promotionType = promotionPiece;
        }

        if (!isKingSafeAfterMove(selected)) {
            setStatus(ChessTexts.KING_IN_CHECK, true);
            return;
        }

        executeMove(selected);

        if (selected.captured != null && selected.captured.isKing()) {
            setOver();
            setStatus(ChessTexts.KING_CAPTURED, false);
            return;
        }

        setStatus(ChessTexts.MOVE_SUCCESS, false);
        isWhiteTurn = !isWhiteTurn;
        updateKingPosition(selected);
    }

    private ChessPiece getPromotionPiece(char type, boolean isWhite) {
        return ChessMoveValidator.getPromotionPiece(type, isWhite);
    }

    private List<ChessMoveCandidate> generateCandidates(Position pos) {
        return ChessMoveValidator.generateCandidates(board, size, lastMove,
                whiteKingMoved, blackKingMoved,
                whiteLeftRookMoved, whiteRightRookMoved,
                blackLeftRookMoved, blackRightRookMoved, pos);
    }

    private boolean isKingSafeAfterMove(ChessMoveCandidate candidate) {
        return ChessMoveValidator.isKingSafeAfterMove(board, size, candidate, lastMove);
    }

    private boolean canCastle(boolean isWhite, boolean kingSide) {
        return ChessMoveValidator.canCastle(board, size, isWhite, kingSide,
                whiteKingMoved, blackKingMoved,
                whiteLeftRookMoved, whiteRightRookMoved,
                blackLeftRookMoved, blackRightRookMoved);
    }

    private void executeMove(ChessMoveCandidate candidate) {
        Position from = candidate.from;
        Position to = candidate.to;
        ChessPiece piece = candidate.piece;
        ChessPiece captured = candidate.captured;
        boolean isEnPassant = candidate.enPassant;
        boolean isCastling = candidate.castling;
        boolean isPromotion = candidate.promotion;
        ChessPiece promotionType = candidate.promotionType;

        board.set(to.row(), to.col(), piece);
        board.set(from.row(), from.col(), ChessPiece.EMPTY);

        if (isEnPassant && lastMove != null) {
            Position capturedPos = lastMove.to;
            board.set(capturedPos.row(), capturedPos.col(), ChessPiece.EMPTY);
        }

        if (isCastling) {
            int fr = from.row();
            int fc = from.col();
            int tc = to.col();
            if (tc > fc) {
                // 短易位：车从 h 列移动到 f 列 (列索引 7 -> 5)
                int rookFromC = size - 1;
                int rookToC = tc - 1;
                board.set(fr, rookToC, board.get(fr, rookFromC));
                board.set(fr, rookFromC, ChessPiece.EMPTY);

                // 标记对应的车为已移动
                if (piece.isWhite()) {
                    if (rookFromC == size - 1) whiteRightRookMoved = true;
                } else {
                    if (rookFromC == size - 1) blackRightRookMoved = true;
                }
            } else {
                // 长易位：车从 a 列移动到 d 列
                int rookFromC = 0;
                int rookToC = tc + 1;
                board.set(fr, rookToC, board.get(fr, rookFromC));
                board.set(fr, rookFromC, ChessPiece.EMPTY);

                // 标记对应的车为已移动
                if (piece.isWhite()) {
                    if (rookFromC == 0) whiteLeftRookMoved = true;
                } else {
                    if (rookFromC == 0) blackLeftRookMoved = true;
                }
            }
        }

        if (isPromotion && promotionType != null) {
            board.set(to.row(), to.col(), promotionType);
        }

        if (piece.isKing()) {
            if (piece.isWhite()) whiteKingMoved = true;
            else blackKingMoved = true;
        }
        if (piece.isRook()) {
            if (piece.isWhite()) {
                if (from.row() == 7 && from.col() == 0) whiteLeftRookMoved = true;
                if (from.row() == 7 && from.col() == 7) whiteRightRookMoved = true;
            } else {
                if (from.row() == 0 && from.col() == 0) blackLeftRookMoved = true;
                if (from.row() == 0 && from.col() == 7) blackRightRookMoved = true;
            }
        }

        boolean doublePawnStep = piece.isPawn() && Math.abs(to.row() - from.row()) == 2;
        lastMove = new ChessMove(from, to, piece, captured, doublePawnStep, isEnPassant, isCastling, promotionType);
    }

    private void updateKingPosition(ChessMoveCandidate candidate) {
        if (candidate.piece.isKing()) {
            if (candidate.piece.isWhite()) {
                whiteKingRow = candidate.to.row();
                whiteKingCol = candidate.to.col();
            } else {
                blackKingRow = candidate.to.row();
                blackKingCol = candidate.to.col();
            }
        }
    }

    private boolean isOutOfBounds(int row, int col) {
        return row < 0 || row >= size || col < 0 || col >= size;
    }

    @Override
    public List<String> getExtraStatusLines() {
        List<String> lines = new ArrayList<>();
        if (isOver()) {
            lines.add(ChessTexts.winnerLine(isWhiteTurn));
        }
        return lines;
    }

    @Override
    public List<String> getAvailableCommands() {
        return List.of(ChessTexts.COMMAND_MOVE);
    }

    @Override
    public int getPlayerCount() {
        return 2;
    }

    @Override
    public int getCurrentPlayerIndex() {
        return isWhiteTurn ? 0 : 1;
    }

    @Override
    public String getPlayerName(int index) {
        return index == 0 ? "白方" : "黑方";
    }

    @Override
    public String getCellChar(int row, int col) {
        return board.get(row, col).displayChessPiece() + " ";
    }


    @Override
    public List<DemoStep> getDemoSteps() {
        return List.of(
                new DemoStep("m e7 e5", "【兵的双步】白方e兵前进两格——兵第一步可走两格"),
                new DemoStep("m e2 e4", "黑方e兵对称应对"),
                new DemoStep("m g8 f6", "【马的走法】白马跳f6——马走\"日\"字，可跳过棋子"),
                new DemoStep("m b1 c3", "黑马跳c3——对称出子"),
                new DemoStep("m f8 c5", "【象的走法】白象斜走c5——象沿斜线移动"),
                new DemoStep("m f1 c4", "黑象斜走c4——双方象对峙"),
                new DemoStep("m e8 g8", "【王翼易位】白方短易位O-O！王向右两格，车跨到王旁——王车易位"),
                new DemoStep("m g1 f3", "黑马跳f3——中局出动"),
                new DemoStep("m b8 c6", "【马的走法】白后翼马跳c6——两匹马均已出动"),
                new DemoStep("m e1 g1", "【王翼易位】黑方短易位——双方均完成王车易位"),
                new DemoStep("m d7 d6", "白d兵走一步——相机巩固中心"),
                new DemoStep("m d2 d3", "黑d兵走一步——为后翼象开路"),
                new DemoStep("m c8 g4", "【象远程牵制】白象远程斜走g4——牵制黑马"),
                new DemoStep("m h2 h3", "黑h兵走一步——驱赶白象"),
                new DemoStep("m g4 f3", "【象吃马】白象吃黑马——象斜线吃子"),
                new DemoStep("m d1 f3", "黑后吃白象——后吃子"),
                new DemoStep("m c6 d4", "【马攻后】白马跳d4威胁黑后——马走日字制造威胁"),
                new DemoStep("m f3 d1", "黑后撤回d1——后沿斜线撤退"),
                new DemoStep("m d4 e2", "【马将军】白马跳e2将军！——马的将军"),
                new DemoStep("m g1 h1", "【王的走法】黑王逃到h1——王每次只能走一格"),
                new DemoStep("m e2 c1", "【马吃象】白马吃黑象——马吃子"),
                new DemoStep("m a1 c1", "【车吃马】黑车横走吃白马——车沿横线吃子"),
                new DemoStep("m a7 a5", "白a边兵推进——准备吃过路兵演示"),
                new DemoStep("m a2 a3", "黑a兵走一步"),
                new DemoStep("m a5 a4", "白兵推进到a4——到达第4横线"),
                new DemoStep("m b2 b4", "黑b兵双步跳到b4——创造过路兵机会！"),
                new DemoStep("m a4 b3", "【吃过路兵！】白a4兵斜吃b4过路兵到b3——过路兵特殊规则"),
                new DemoStep("m g2 g4", "黑g兵双步推进"),
                new DemoStep("m b3 b2", "白兵推进到b2——逼近底线，即将升变！"),
                new DemoStep("m g4 g5", "黑g兵推进一步"),
                new DemoStep("m b2 c1 q", "【兵升变为后！】白兵斜吃黑车冲到底线，升变为后——兵到底线可变后/车/象/马")
        );
    }

    @Override
    public int getDemoStepDelayMs() {
        return 1200;
    }
}
