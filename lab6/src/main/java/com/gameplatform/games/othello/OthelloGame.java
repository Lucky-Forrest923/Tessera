package com.gameplatform.games.othello;

import com.gameplatform.model.BaseGame;
import com.gameplatform.model.Position;
import com.gameplatform.model.TurnBasedGame;

import java.util.ArrayList;
import java.util.List;

public abstract class OthelloGame extends BaseGame implements TurnBasedGame {
    protected final Board board;
    private Piece turn = Piece.BLACK;
    protected int blackScore;
    protected int whiteScore;

    public OthelloGame(int size) {
        super(size);
        this.board = new Board(size);
        initializeBoard();
        if (usesScores()) {
            refreshScores();
        }
    }

    private void initializeBoard() {
        int mid = size / 2;
        board.setPiece(mid - 1, mid - 1, Piece.WHITE);
        board.setPiece(mid - 1, mid, Piece.BLACK);
        board.setPiece(mid, mid - 1, Piece.BLACK);
        board.setPiece(mid, mid, Piece.WHITE);
    }

    @Override
    public boolean processCommand(String cmd) {
        if (isOver()) {
            setStatus(OthelloTexts.GAME_OVER, true);
            return true;
        }

        Position position = parsePosition(cmd);
        if (position != null) {
            if (board.isOutOfBounds(position.row(), position.col())) {
                setStatus(OthelloTexts.OUT_OF_BOUNDS, true);
            } else if (board.isOccupied(position.row(), position.col())) {
                setStatus(OthelloTexts.OCCUPIED, true);
            } else {
                if (doPlay(position)) {
                    if (usesScores()) {
                        refreshScores();
                    }
                }
            }
            return true;
        }
        return false;
    }

    protected abstract boolean doPlay(Position position);

    protected boolean usesScores() {
        return true;
    }

    public Piece getTurn() {
        return turn;
    }

    public void switchTurn() {
        turn = turn.opponent();
    }

    @Override
    public void onCellClick(int row, int col, boolean isSecondary, boolean isFlagMode) {
        if (isSecondary || isFlagMode) {
            setStatus("该游戏不支持右键操作", true);
            return;
        }
        String cmd = (char)('a' + col) + String.valueOf(row + 1);
        processCommand(cmd);
    }

    public void refreshScores() {
        if (!usesScores()) {
            return;
        }
        blackScore = board.getCount(Piece.BLACK);
        whiteScore = board.getCount(Piece.WHITE);
    }

    @Override
    public List<String> getExtraStatusLines() {
        List<String> lines = new ArrayList<>();
        if (usesScores()) {
            lines.add(OthelloTexts.scoreLine(blackScore, whiteScore));
            if (isOver()) {
                if (blackScore > whiteScore) lines.add(OthelloTexts.RESULT_BLACK_WIN);
                else if (whiteScore > blackScore) lines.add(OthelloTexts.RESULT_WHITE_WIN);
                else lines.add(OthelloTexts.RESULT_DRAW);
            }
        }
        return lines;
    }

    @Override
    public int getPlayerCount() {
        return 2;
    }

    @Override
    public int getCurrentPlayerIndex() {
        return turn == Piece.BLACK ? 0 : 1;
    }

    @Override
    public String getPlayerName(int index) {
        return index == 0 ? "黑方" : "白方";
    }

    @Override
    public String getCellChar(int row, int col) {
        Piece piece = board.getPiece(row, col);
        return switch (piece) {
            case BLACK -> "● ";
            case WHITE -> "○ ";
            case EMPTY -> "· ";
        };
    }
}