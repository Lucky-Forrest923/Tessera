package com.gameplatform.games.chess;

import com.gameplatform.model.Position;

import java.util.ArrayList;
import java.util.List;

class ChessMoveValidator {

    interface BoardState {
        ChessPiece get(int row, int col);
        boolean isOutOfBounds(int row, int col);
    }

    private ChessMoveValidator() {}

    static List<ChessMoveCandidate> generateCandidates(ChessBoard board, int size, ChessMove lastMove,
                                                        boolean whiteKingMoved, boolean blackKingMoved,
                                                        boolean whiteLeftRookMoved, boolean whiteRightRookMoved,
                                                        boolean blackLeftRookMoved, boolean blackRightRookMoved,
                                                        Position pos) {
        List<ChessMoveCandidate> candidates = new ArrayList<>();
        int row = pos.row();
        int col = pos.col();
        ChessPiece piece = board.get(row, col);
        if (piece == null || piece.isEmpty()) return candidates;

        // Knight
        if (piece.isKnight()) {
            int[][] moveset = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
            for (int[] m : moveset) {
                int nr = row + m[0], nc = col + m[1];
                if (isOutOfBounds(nr, nc, size)) continue;
                ChessPiece target = board.get(nr, nc);
                if (target.isEmpty() || target.isWhite() != piece.isWhite()) {
                    candidates.add(new ChessMoveCandidate(piece, new Position(row, col), new Position(nr, nc),
                            target, false, false, false, false, null));
                }
            }
            return candidates;
        }

        // King
        if (piece.isKing()) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = row + dr, nc = col + dc;
                    if (isOutOfBounds(nr, nc, size)) continue;
                    ChessPiece target = board.get(nr, nc);
                    if (target.isEmpty() || target.isWhite() != piece.isWhite()) {
                        candidates.add(new ChessMoveCandidate(piece, new Position(row, col), new Position(nr, nc),
                                target, false, false, false, false, null));
                    }
                }
            }
            // Castling
            int kingRow = piece.isWhite() ? 7 : 0;
            if (!whiteKingMoved && piece.isWhite()) {
                if (canCastle(board, size, true, true,
                        whiteKingMoved, blackKingMoved,
                        whiteLeftRookMoved, whiteRightRookMoved,
                        blackLeftRookMoved, blackRightRookMoved)) {
                    candidates.add(new ChessMoveCandidate(piece, new Position(kingRow, 4), new Position(kingRow, 6),
                            null, false, false, true, false, null));
                }
                if (canCastle(board, size, true, false,
                        whiteKingMoved, blackKingMoved,
                        whiteLeftRookMoved, whiteRightRookMoved,
                        blackLeftRookMoved, blackRightRookMoved)) {
                    candidates.add(new ChessMoveCandidate(piece, new Position(kingRow, 4), new Position(kingRow, 2),
                            null, false, false, true, false, null));
                }
            }
            if (!blackKingMoved && !piece.isWhite()) {
                if (canCastle(board, size, false, true,
                        whiteKingMoved, blackKingMoved,
                        whiteLeftRookMoved, whiteRightRookMoved,
                        blackLeftRookMoved, blackRightRookMoved)) {
                    candidates.add(new ChessMoveCandidate(piece, new Position(0, 4), new Position(0, 6),
                            null, false, false, true, false, null));
                }
                if (canCastle(board, size, false, false,
                        whiteKingMoved, blackKingMoved,
                        whiteLeftRookMoved, whiteRightRookMoved,
                        blackLeftRookMoved, blackRightRookMoved)) {
                    candidates.add(new ChessMoveCandidate(piece, new Position(0, 4), new Position(0, 2),
                            null, false, false, true, false, null));
                }
            }
            return candidates;
        }

        // Rook, Bishop, Queen
        if (piece.isRook() || piece.isBishop() || piece.isQueen()) {
            int[][] directions;
            if (piece.isRook())
                directions = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            else if (piece.isBishop())
                directions = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            else
                directions = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

            for (int[] d : directions) {
                int nr = row + d[0], nc = col + d[1];
                while (!isOutOfBounds(nr, nc, size)) {
                    ChessPiece target = board.get(nr, nc);
                    if (target.isEmpty()) {
                        candidates.add(new ChessMoveCandidate(piece, new Position(row, col), new Position(nr, nc),
                                null, false, false, false, false, null));
                    } else {
                        if (target.isWhite() != piece.isWhite()) {
                            candidates.add(new ChessMoveCandidate(piece, new Position(row, col), new Position(nr, nc),
                                    target, false, false, false, false, null));
                        }
                        break;
                    }
                    nr += d[0];
                    nc += d[1];
                }
            }
            return candidates;
        }

        // Pawn
        if (piece.isPawn()) {
            int dir = piece.isWhite() ? -1 : 1;
            int startRow = piece.isWhite() ? 6 : 1;
            int fr = row, fc = col;

            int nr = fr + dir, nc = fc;
            if (!isOutOfBounds(nr, nc, size) && board.get(nr, nc).isEmpty()) {
                boolean isPromo = (piece.isWhite() && nr == 0) || (!piece.isWhite() && nr == size - 1);
                candidates.add(new ChessMoveCandidate(piece, new Position(fr, fc), new Position(nr, nc),
                        null, false, false, false, isPromo, null));
                int nr2 = fr + 2 * dir;
                if (fr == startRow && !isOutOfBounds(nr2, nc, size) && board.get(nr, nc).isEmpty() && board.get(nr2, nc).isEmpty()) {
                    candidates.add(new ChessMoveCandidate(piece, new Position(fr, fc), new Position(nr2, nc),
                            null, true, false, false, false, null));
                }
            }

            for (int dc : new int[]{-1, 1}) {
                int cr = fr + dir, cc = fc + dc;
                if (isOutOfBounds(cr, cc, size)) continue;
                ChessPiece target = board.get(cr, cc);
                if (!target.isEmpty() && target.isWhite() != piece.isWhite()) {
                    boolean isPromo = (piece.isWhite() && cr == 0) || (!piece.isWhite() && cr == size - 1);
                    candidates.add(new ChessMoveCandidate(piece, new Position(fr, fc), new Position(cr, cc),
                            target, false, false, false, isPromo, null));
                }
                if (lastMove != null && lastMove.doublePawnStep && lastMove.to.row() == fr && lastMove.to.col() == cc) {
                    if (board.get(cr, cc).isEmpty()) {
                        ChessPiece capturedPawn = board.get(lastMove.to.row(), lastMove.to.col());
                        candidates.add(new ChessMoveCandidate(piece, new Position(fr, fc), new Position(cr, cc),
                                capturedPawn, false, true, false, false, null));
                    }
                }
            }
        }

        return candidates;
    }

    static boolean isKingSafeAfterMove(ChessBoard board, int size, ChessMoveCandidate candidate, ChessMove lastMove) {
        ChessPiece[][] copy = board.copy();
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
            int rookFromC, rookToC;
            if (tc > fc) {
                rookFromC = size - 1;
                rookToC = tc - 1;
            } else {
                rookFromC = 0;
                rookToC = tc + 1;
            }
            copy[fr][rookToC] = copy[fr][rookFromC];
            copy[fr][rookFromC] = ChessPiece.EMPTY;
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
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
        }
        if (kingRow == -1) return false;
        return !isSquareAttacked(boardState(copy, size), kingRow, kingCol, !isWhite, size);
    }

    static boolean isSquareAttacked(BoardState state, int row, int col, boolean byWhite, int size) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                ChessPiece p = state.get(r, c);
                if (p == null || p.isEmpty()) continue;
                if (p.isWhite() != byWhite) continue;
                if (canAttackSquare(state, p, r, c, row, col)) return true;
            }
        }
        return false;
    }

    static boolean canAttackSquare(BoardState state, ChessPiece piece, int sr, int sc, int tr, int tc) {
        int dr = Integer.compare(tr - sr, 0);
        int dc = Integer.compare(tc - sc, 0);
        if (dr == 0 && dc == 0) return false;

        if (piece.isKnight()) {
            int[][] offsets = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
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
                if (state.isOutOfBounds(cr, cc)) return false;
                if (!state.get(cr, cc).isEmpty()) return false;
                cr += stepR;
                cc += stepC;
            }
            return true;
        }
        return false;
    }

    static boolean canCastle(ChessBoard board, int size,
                              boolean isWhite, boolean kingSide,
                              boolean whiteKingMoved, boolean blackKingMoved,
                              boolean whiteLeftRookMoved, boolean whiteRightRookMoved,
                              boolean blackLeftRookMoved, boolean blackRightRookMoved) {
        int row = isWhite ? 7 : 0;
        int rookCol = kingSide ? size - 1 : 0;
        int[] passCols = kingSide ? new int[]{5, 6} : new int[]{3, 2};
        boolean rookMoved = isWhite
                ? (kingSide ? whiteRightRookMoved : whiteLeftRookMoved)
                : (kingSide ? blackRightRookMoved : blackLeftRookMoved);
        if (rookMoved) return false;
        ChessPiece rook = board.get(row, rookCol);
        if (rook == null || !rook.isRook() || rook.isWhite() != isWhite) return false;
        if (isSquareAttacked(boardState(board, size), row, 4, !isWhite, size)) return false;
        if (!kingSide && !board.get(row, 1).isEmpty()) return false;
        for (int c : passCols) {
            if (!board.get(row, c).isEmpty()) return false;
            if (isSquareAttacked(boardState(board, size), row, c, !isWhite, size)) return false;
        }
        return true;
    }

    static ChessPiece getPromotionPiece(char type, boolean isWhite) {
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

    static BoardState boardState(ChessPiece[][] cells, int size) {
        return new BoardState() {
            @Override
            public ChessPiece get(int row, int col) { return cells[row][col]; }

            @Override
            public boolean isOutOfBounds(int row, int col) {
                return row < 0 || row >= size || col < 0 || col >= size;
            }
        };
    }

    static BoardState boardState(ChessBoard board, int size) {
        return new BoardState() {
            @Override
            public ChessPiece get(int row, int col) { return board.get(row, col); }

            @Override
            public boolean isOutOfBounds(int row, int col) {
                return board.isOutOfBounds(row, col);
            }
        };
    }

    private static boolean isOutOfBounds(int row, int col, int size) {
        return row < 0 || row >= size || col < 0 || col >= size;
    }
}
