package com.othello.model;

import java.util.List;

public abstract class BaseGame {
    protected final Board board;
    private Piece turn = Piece.BLACK;
    private boolean over = false;
    private GameStatusCode code = GameStatusCode.NONE;
    private int blackScore;
    private int whiteScore;

    public BaseGame(int size) {
        this.board = new Board(size);
        initializeBoard();
        refreshScores();
        setCode(GameStatusCode.GAME_START);
    }

    protected void initializeBoard() {
        int m = board.getSize() / 2;
        board.setPiece(m - 1, m - 1, Piece.WHITE);
        board.setPiece(m, m, Piece.WHITE);
        board.setPiece(m - 1, m, Piece.BLACK);
        board.setPiece(m, m - 1, Piece.BLACK);
    }

    public final void play(Position position) {
        if (isOver()) {
            setCode(GameStatusCode.GAME_ALREADY_OVER);
            return;
        }
        if (position == null) {
            setCode(GameStatusCode.INVALID_COORDINATE);
            return;
        }
        if (board.isOutOfBounds(position.row(), position.col())) {
            setCode(GameStatusCode.OUT_OF_BOUNDS);
            return;
        }
        if (board.isOccupied(position.row(), position.col())) {
            setCode(GameStatusCode.OCCUPIED);
            return;
        }
        if (doPlay(position)) {
            refreshScores();
        }
    }

    protected abstract boolean doPlay(Position position);

    public abstract List<Position> getLegalMoves();

    public abstract String getType();

    public void pass() {
        throw new UnsupportedOperationException("当前游戏不支持 pass");
    }

    public Board getBoard() {
        return board;
    }

    public Piece getTurn() {
        return turn;
    }

    public void switchTurn() {
        turn = turn.opponent();
    }

    public boolean isOver() {
        return over;
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    public GameStatusCode getCode() {
        return code;
    }

    public void setCode(GameStatusCode code) {
        this.code = code;
    }

    public int getBlackScore() {
        return blackScore;
    }

    public int getWhiteScore() {
        return whiteScore;
    }

    protected void refreshScores() {
        blackScore = board.getCount(Piece.BLACK);
        whiteScore = board.getCount(Piece.WHITE);
    }

    /**
     * 将最近一次操作的状态码翻译为面向用户的提示文本。
     * 由具体的游戏逻辑在 play()/pass() 内部设置 code 后调用。
     */
    public String getStatusMessage() {
        return switch (code) {
            case INVALID_COORDINATE -> "输入格式有误！请输入正确的位置格式，如 D4";
            case OUT_OF_BOUNDS -> "坐标超出范围！";
            case OCCUPIED -> "该位置已有棋子！";
            case ILLEGAL_MOVE -> "非法落子！";
            case GAME_ALREADY_OVER -> "游戏已结束。";
            case PASS_NOT_ALLOWED -> "不允许放弃行棋！";
            case MOVE_ACCEPTED -> "有效落子！轮到下一位玩家。";
            case PASS -> "当前玩家无合法落子，请按 pass。";
            case GAME_OVER_FULL -> "游戏结束：棋盘已满。";
            case GAME_OVER_NO_MOVES -> "游戏结束：双方无合法位置。";
            default -> "";
        };
    }

    /**
     * 判断当前状态码是否代表一次错误/非法操作。
     */
    public boolean isStatusError() {
        return switch (code) {
            case INVALID_COORDINATE, OUT_OF_BOUNDS, OCCUPIED,
                 ILLEGAL_MOVE, GAME_ALREADY_OVER, PASS_NOT_ALLOWED -> true;
            default -> false;
        };
    }
}