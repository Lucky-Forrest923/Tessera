package com.gameplatform.games.chess;

import com.gameplatform.model.BaseGame;
import com.gameplatform.model.Position;
import com.gameplatform.model.TurnBasedGame;

import java.util.ArrayList;
import java.util.List;

public class ChessGame extends BaseGame implements TurnBasedGame {
    private final ChessBoard board;
    private boolean isWhiteTurn = true;

    private Move lastMove = null;

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

    private static final class Move {
        final Position from;
        final Position to;
        final ChessPiece moved;
        final ChessPiece captured;
        final boolean doublePawnStep;
        final boolean enPassant;
        final boolean castling;
        final ChessPiece promotion;

        Move(Position from, Position to, ChessPiece moved, ChessPiece captured,
             boolean doublePawnStep, boolean enPassant, boolean castling, ChessPiece promotion) {
            this.from = from;
            this.to = to;
            this.moved = moved;
            this.captured = captured;
            this.doublePawnStep = doublePawnStep;
            this.enPassant = enPassant;
            this.castling = castling;
            this.promotion = promotion;
        }
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
    public boolean processCommand(String cmd) {
        if (isOver()) {
            setStatus(ChessTexts.GAME_OVER, true);
            return true;
        }

        if (cmd.matches("(?i)^(m|move)\\s+[a-z0-9]+\\s+[a-z0-9]+(\\s+[qrbn])?$") ) {
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

        List<MoveCandidate> candidates = generateCandidates(from);
        MoveCandidate selected = null;
        for (MoveCandidate mc : candidates) {
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
        char lower = Character.toLowerCase(type);
        if (isWhite) {
            return switch (lower) {
                case 'q' -> ChessPiece.WQUEEN;
                case 'r' -> ChessPiece.WROOK;
                case 'n' -> ChessPiece.WKNIGHT;
                case 'b' -> ChessPiece.WBISHOP;
                default -> null;
            };
        } else {
            return switch (lower) {
                case 'q' -> ChessPiece.BQUEEN;
                case 'r' -> ChessPiece.BROOK;
                case 'n' -> ChessPiece.BKNIGHT;
                case 'b' -> ChessPiece.BBISHOP;
                default -> null;
            };
        }
    }

    private List<MoveCandidate> generateCandidates(Position pos) {
        List<MoveCandidate> candidates = new ArrayList<>();
        int row = pos.row();
        int col = pos.col();
        ChessPiece piece = board.get(row, col);
        if (piece == null || piece.isEmpty()) return candidates;

        // 马
        if (piece.isKnight()) {
            int[][] moveset = {{-2,-1}, {-2,1}, {-1,-2}, {-1,2}, {1,-2}, {1,2}, {2,-1}, {2,1}};
            for (int[] m : moveset) {
                int nr = row + m[0], nc = col + m[1];
                if (isOutOfBounds(nr, nc)) continue;
                ChessPiece target = board.get(nr, nc);
                if (target.isEmpty() || target.isWhite() != piece.isWhite()) {
                    candidates.add(new MoveCandidate(piece, new Position(row, col), new Position(nr, nc),
                            target, false, false, false, false, null));
                }
            }
            return candidates;
        }

        // 王
        if (piece.isKing()) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = row + dr, nc = col + dc;
                    if (isOutOfBounds(nr, nc)) continue;
                    ChessPiece target = board.get(nr, nc);
                    if (target.isEmpty() || target.isWhite() != piece.isWhite()) {
                        candidates.add(new MoveCandidate(piece, new Position(row, col), new Position(nr, nc),
                                target, false, false, false, false, null));
                    }
                }
            }
            // 易位
            if (piece.isWhite()) {
                if (!whiteKingMoved && canCastle(piece.isWhite(), true)) {
                    candidates.add(new MoveCandidate(piece, new Position(7,4), new Position(7,6),
                            null, false, false, true, false, null));
                }
                if (!whiteKingMoved && canCastle(piece.isWhite(), false)) {
                    candidates.add(new MoveCandidate(piece, new Position(7,4), new Position(7,2),
                            null, false, false, true, false, null));
                }
            } else {
                if (!blackKingMoved && canCastle(piece.isWhite(), true)) {
                    candidates.add(new MoveCandidate(piece, new Position(0,4), new Position(0,6),
                            null, false, false, true, false, null));
                }
                if (!blackKingMoved && canCastle(piece.isWhite(), false)) {
                    candidates.add(new MoveCandidate(piece, new Position(0,4), new Position(0,2),
                            null, false, false, true, false, null));
                }
            }
            return candidates;
        }

        // 车、象、后
        if (piece.isRook() || piece.isBishop() || piece.isQueen()) {
            int[][] directions;
            if (piece.isRook()) directions = new int[][]{{-1,0},{1,0},{0,-1},{0,1}};
            else if (piece.isBishop()) directions = new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}};
            else directions = new int[][]{{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}};

            for (int[] d : directions) {
                int nr = row + d[0], nc = col + d[1];
                while (!isOutOfBounds(nr, nc)) {
                    ChessPiece target = board.get(nr, nc);
                    if (target.isEmpty()) {
                        candidates.add(new MoveCandidate(piece, new Position(row, col), new Position(nr, nc),
                                null, false, false, false, false, null));
                    } else {
                        if (target.isWhite() != piece.isWhite()) {
                            candidates.add(new MoveCandidate(piece, new Position(row, col), new Position(nr, nc),
                                    target, false, false, false, false, null));
                        }
                        break;
                    }
                    nr += d[0]; nc += d[1];
                }
            }
            return candidates;
        }

        // 兵
        if (piece.isPawn()) {
            int dir = piece.isWhite() ? -1 : 1;
            int startRow = piece.isWhite() ? 6 : 1;
            int fr = row, fc = col;

            // 直走一步
            int nr = fr + dir, nc = fc;
            if (!isOutOfBounds(nr, nc) && board.get(nr, nc).isEmpty()) {
                boolean isPromotion = (piece.isWhite() && nr == 0) || (!piece.isWhite() && nr == size-1);
                candidates.add(new MoveCandidate(piece, new Position(fr, fc), new Position(nr, nc),
                        null, false, false, false, isPromotion, null));
                // 直走两步（起始位置）
                int nr2 = fr + 2*dir;
                if (fr == startRow && !isOutOfBounds(nr2, nc) && board.get(nr2, nc).isEmpty()) {
                    candidates.add(new MoveCandidate(piece, new Position(fr, fc), new Position(nr2, nc),
                            null, true, false, false, false, null));
                }
            }

            // 斜吃
            for (int dc : new int[]{-1, 1}) {
                int cr = fr + dir, cc = fc + dc;
                if (isOutOfBounds(cr, cc)) continue;
                ChessPiece target = board.get(cr, cc);
                if (!target.isEmpty() && target.isWhite() != piece.isWhite()) {
                    boolean isPromotion = (piece.isWhite() && cr == 0) || (!piece.isWhite() && cr == size-1);
                    candidates.add(new MoveCandidate(piece, new Position(fr, fc), new Position(cr, cc),
                            target, false, false, false, isPromotion, null));
                }
                // 吃过路兵
                if (lastMove != null && lastMove.doublePawnStep && lastMove.to.row() == fr && lastMove.to.col() == cc) {
                    ChessPiece capturedPawn = board.get(lastMove.to.row(), lastMove.to.col());
                    candidates.add(new MoveCandidate(piece, new Position(fr, fc), new Position(cr, cc),
                            capturedPawn, false, true, false, false, null));
                }
            }
        }

        return candidates;
    }

    private boolean isKingSafeAfterMove(MoveCandidate candidate) {
        ChessPiece[][] copy = copyBoard();
        ChessPiece movedPiece = candidate.piece;
        Position from = candidate.from;
        Position to = candidate.to;
        boolean isEnPassant = candidate.enPassant;
        boolean isCastling = candidate.castling;

        copy[to.row()][to.col()] = movedPiece;
        copy[from.row()][from.col()] = ChessPiece.EMPTY;

        if (isEnPassant && lastMove != null) {
            Position capturedPos = lastMove.to;
            copy[capturedPos.row()][capturedPos.col()] = ChessPiece.EMPTY;
        }

        if (isCastling) {
            int fr = from.row();
            int fc = from.col();
            int tc = to.col();
            if (tc > fc) {
                int rookFromC = size-1;
                int rookToC = tc - 1;
                copy[fr][rookToC] = copy[fr][rookFromC];
                copy[fr][rookFromC] = ChessPiece.EMPTY;
            } else {
                int rookFromC = 0;
                int rookToC = tc + 1;
                copy[fr][rookToC] = copy[fr][rookFromC];
                copy[fr][rookFromC] = ChessPiece.EMPTY;
            }
        }

        if (candidate.promotion && candidate.promotionType != null) {
            copy[to.row()][to.col()] = candidate.promotionType;
        }

        int kingRow = -1, kingCol = -1;
        boolean isWhite = movedPiece.isWhite();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                ChessPiece p = copy[r][c];
                if (p != null && p.isKing() && p.isWhite() == isWhite) {
                    kingRow = r; kingCol = c; break;
                }
            }
        }
        if (kingRow == -1) return false;
        return !isSquareAttacked(copy, kingRow, kingCol, !isWhite);
    }

    private boolean isSquareAttacked(ChessPiece[][] board, int row, int col, boolean byWhite) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                ChessPiece p = board[r][c];
                if (p == null || p.isEmpty()) continue;
                if (p.isWhite() != byWhite) continue;
                if (canAttackSquare(board, p, r, c, row, col)) return true;
            }
        }
        return false;
    }

    private boolean canAttackSquare(ChessPiece[][] board, ChessPiece piece, int sr, int sc, int tr, int tc) {
        int dr = Integer.compare(tr - sr, 0);
        int dc = Integer.compare(tc - sc, 0);
        if (dr == 0 && dc == 0) return false;

        if (piece.isKnight()) {
            int[][] offsets = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
            for (int[] off : offsets) {
                if (sr + off[0] == tr && sc + off[1] == tc) return true;
            }
            return false;
        }
        if (piece.isKing()) {
            return Math.abs(sr - tr) <= 1 && Math.abs(sc - tc) <= 1;
        }
        if (piece.isPawn()) {
            int dir = piece.isWhite() ? -1 : 1;
            return (tr == sr + dir) && Math.abs(tc - sc) == 1;
        }
        if (piece.isRook() || piece.isBishop() || piece.isQueen()) {
            if (piece.isRook() && dr != 0 && dc != 0) return false;
            if (piece.isBishop() && (dr == 0 || dc == 0)) return false;
            int stepR = dr, stepC = dc;
            int cr = sr + stepR, cc = sc + stepC;
            while (cr != tr || cc != tc) {
                if (cr < 0 || cr >= size || cc < 0 || cc >= size) return false;
                if (!board[cr][cc].isEmpty()) return false;
                cr += stepR; cc += stepC;
            }
            return true;
        }
        return false;
    }

    private ChessPiece[][] copyBoard() {
        return board.copy();
    }

    private void executeMove(MoveCandidate candidate) {
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
                int rookFromC = size-1;
                int rookToC = tc - 1;
                board.set(fr, rookToC, board.get(fr, rookFromC));
                board.set(fr, rookFromC, ChessPiece.EMPTY);
            } else {
                int rookFromC = 0;
                int rookToC = tc + 1;
                board.set(fr, rookToC, board.get(fr, rookFromC));
                board.set(fr, rookFromC, ChessPiece.EMPTY);
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
        lastMove = new Move(from, to, piece, captured, doublePawnStep, isEnPassant, isCastling, promotionType);
    }

    private void updateKingPosition(MoveCandidate candidate) {
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

    private boolean canCastle(boolean isWhite, boolean kingSide) {
        int row = isWhite ? 7 : 0;
        int kingCol = 4;
        int rookCol = kingSide ? size - 1 : 0;
        int[] passCols = kingSide ? new int[]{5, 6} : new int[]{3, 2};
        boolean rookMoved = isWhite
                ? (kingSide ? whiteRightRookMoved : whiteLeftRookMoved)
                : (kingSide ? blackRightRookMoved : blackLeftRookMoved);
        if (rookMoved) return false;
        ChessPiece rook = board.get(row, rookCol);
        if (rook == null || !rook.isRook() || rook.isWhite() != isWhite) return false;
        if (isSquareAttacked(board, row, kingCol, !isWhite)) return false;
        for (int c : passCols) {
            if (!board.get(row, c).isEmpty()) return false;
            if (isSquareAttacked(board, row, c, !isWhite)) return false;
        }
        return true;
    }

    private boolean isSquareAttacked(ChessBoard board, int row, int col, boolean byWhite) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                ChessPiece p = board.get(r, c);
                if (p == null || p.isEmpty()) continue;
                if (p.isWhite() != byWhite) continue;
                if (canAttackSquare(board, p, r, c, row, col)) return true;
            }
        }
        return false;
    }

    private boolean canAttackSquare(ChessBoard board, ChessPiece piece, int sr, int sc, int tr, int tc) {
        int dr = Integer.compare(tr - sr, 0);
        int dc = Integer.compare(tc - sc, 0);
        if (dr == 0 && dc == 0) return false;

        if (piece.isKnight()) {
            int[][] offsets = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
            for (int[] off : offsets) {
                if (sr + off[0] == tr && sc + off[1] == tc) return true;
            }
            return false;
        }
        if (piece.isKing()) {
            return Math.abs(sr - tr) <= 1 && Math.abs(sc - tc) <= 1;
        }
        if (piece.isPawn()) {
            int dir = piece.isWhite() ? -1 : 1;
            return (tr == sr + dir) && Math.abs(tc - sc) == 1;
        }
        if (piece.isRook() || piece.isBishop() || piece.isQueen()) {
            if (piece.isRook() && dr != 0 && dc != 0) return false;
            if (piece.isBishop() && (dr == 0 || dc == 0)) return false;
            int stepR = dr, stepC = dc;
            int cr = sr + stepR, cc = sc + stepC;
            while (cr != tr || cc != tc) {
                if (board.isOutOfBounds(cr, cc)) return false;
                if (!board.get(cr, cc).isEmpty()) return false;
                cr += stepR; cc += stepC;
            }
            return true;
        }
        return false;
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
    public List<Position> getLegalMoves() {
        return new ArrayList<>();
    }

    /**
     * 重写坐标解析：将国际象棋坐标(a1~h8)正确映射到内部棋盘行号。
     * BaseGame.parsePosition 将 "1" 映射到 row=0，但棋盘上 row=0 是黑方底线(第8横线)。
     * 正确映射：第N横线 → row = 8 - N（即 "1"→row=7, "8"→row=0）。
     */
    @Override
    protected Position parsePosition(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase();
        if (s.matches("^[a-z]\\d+$")) {
            int col = s.charAt(0) - 'a';
            int rank = Integer.parseInt(s.substring(1));
            int row = getSize() - rank;
            return new Position(row, col);
        }
        if (s.matches("^\\d+[a-z]$")) {
            int rank = Integer.parseInt(s.replaceAll("[a-z]", ""));
            int row = getSize() - rank;
            int col = s.charAt(s.length() - 1) - 'a';
            return new Position(row, col);
        }
        return null;
    }

    @Override
    public List<DemoStep> getDemoSteps() {
        return List.of(
                // ===== 开局：基本行棋规则 =====
                new DemoStep("m e2 e4", "【兵的双步】白方e兵前进两格——兵第一步可走两格"),
                new DemoStep("m e7 e5", "黑方e兵对称应对"),
                new DemoStep("m g1 f3", "【马的走法】白马跳f3——马走\"日\"字，可跳过棋子"),
                new DemoStep("m b8 c6", "黑马跳c6——对称出子"),
                new DemoStep("m f1 c4", "【象的走法】白象斜走c4——象沿斜线移动"),
                new DemoStep("m f8 c5", "黑象斜走c5——双方象对峙"),
                // ===== 王车易位 =====
                new DemoStep("m e1 g1", "【王翼易位】白方短易位O-O！王向右两格，车跨到王旁——王车易位"),
                new DemoStep("m g8 f6", "黑马跳f6——中局出动"),
                new DemoStep("m b1 c3", "【马的走法】白后翼马跳c3——两匹马均已出动"),
                new DemoStep("m e8 g8", "【王翼易位】黑方短易位——双方均完成王车易位"),
                new DemoStep("m d2 d3", "白d兵走一步——相机巩固中心"),
                new DemoStep("m d7 d6", "黑d兵走一步——为后翼象开路"),
                // ===== 吃子战术 =====
                new DemoStep("m c1 g5", "【象远程牵制】白象远程斜走g5——牵制黑马"),
                new DemoStep("m h7 h6", "黑h兵走一步——驱赶白象"),
                new DemoStep("m g5 f6", "【象吃马】白象吃黑马——象斜线吃子"),
                new DemoStep("m d8 f6", "黑后吃白象——后吃子"),
                new DemoStep("m c3 d5", "【马攻后】白马跳d5威胁黑后——马走日字制造威胁"),
                new DemoStep("m f6 d8", "黑后撤回d8——后沿斜线撤退"),
                new DemoStep("m d5 e7", "【马将军】白马跳e7将军！——马的将军"),
                new DemoStep("m g8 h8", "【王的走法】黑王逃到h8——王每次只能走一格"),
                new DemoStep("m e7 c8", "【马吃象】白马吃黑象——马吃子"),
                new DemoStep("m a8 c8", "【车吃马】黑车横走吃白马——车沿横线吃子"),
                // ===== 吃过路兵 =====
                new DemoStep("m a2 a4", "白a边兵推进——准备吃过路兵演示"),
                new DemoStep("m a7 a6", "黑a兵走一步"),
                new DemoStep("m a4 a5", "白兵推进到a5——到达第5横线"),
                new DemoStep("m b7 b5", "黑b兵双步跳到b5——创造过路兵机会！"),
                new DemoStep("m a5 b6", "【吃过路兵！】白a5兵斜吃b5过路兵到b6——过路兵特殊规则"),
                new DemoStep("m g7 g5", "黑g兵双步推进"),
                // ===== 兵升变 =====
                new DemoStep("m b6 b7", "白兵推进到b7——逼近底线，即将升变！"),
                new DemoStep("m g5 g4", "黑g兵推进一步"),
                new DemoStep("m b7 c8 q", "【兵升变为后！】白兵斜吃黑车冲到底线，升变为后——兵到底线可变后/车/象/马")
        );
    }

    @Override
    public int getDemoStepDelayMs() {
        return 1200;
    }

    private static class MoveCandidate {
        final ChessPiece piece;
        final Position from;
        final Position to;
        final ChessPiece captured;
        final boolean doublePawnStep;
        final boolean enPassant;
        final boolean castling;
        final boolean promotion;
        ChessPiece promotionType;

        MoveCandidate(ChessPiece piece, Position from, Position to, ChessPiece captured,
                      boolean doublePawnStep, boolean enPassant, boolean castling,
                      boolean promotion, ChessPiece promotionType) {
            this.piece = piece;
            this.from = from;
            this.to = to;
            this.captured = captured;
            this.doublePawnStep = doublePawnStep;
            this.enPassant = enPassant;
            this.castling = castling;
            this.promotion = promotion;
            this.promotionType = promotionType;
        }
    }
}