package com.othello.model;

import com.othello.text.GameTexts;

public abstract class OthelloGame extends BaseGame {
    protected final Board board;
    private Piece turn = Piece.BLACK;
    private int blackScore;
    private int whiteScore;

    /**
     * 创建一个黑白棋类游戏，并完成初始棋盘布子。
     *
     * @param size 棋盘大小
     */
    public OthelloGame(int size) {
        super(size);
        this.board = new Board(size);
        initializeBoard();
        refreshScores();
        setCode(GameStatusCode.GAME_START);
    }

    /**
     * 初始化黑白棋开局的四个子。
     */
    private void initializeBoard() {
        int mid = size / 2;
        board.setPiece(mid - 1, mid - 1, Piece.WHITE);
        board.setPiece(mid - 1, mid, Piece.BLACK);
        board.setPiece(mid, mid - 1, Piece.BLACK);
        board.setPiece(mid, mid, Piece.WHITE);
    }

    /**
     * 执行一次黑白棋落子。
     *
     * @param position 目标位置
     */
    @Override
    public void play(Position position) {
        if (isOver()) {
            setCode(GameStatusCode.GAME_ALREADY_OVER);
            return;
        }
        if (position == null) {
            setCode(GameStatusCode.INVALID_COORDINATE);
            return;
        }
        if (board.isOutOfBounds(position.row(), position.col())) {
            setCode(GameStatusCode.INVALID_COORDINATE);
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

    /**
     * 返回当前黑白棋状态消息。
     *
     * @return 状态消息文本
     */
    @Override
    public String getStatusMessage() {
        return GameTexts.othelloStatusMessage(this.code);
    }

    /**
     * 执行具体的落子逻辑，由子类实现。
     *
     * @param position 目标位置
     * @return 如果本次落子有效则返回 {@code true}
     */
    protected abstract boolean doPlay(Position position);

    /**
     * 执行 pass 操作。
     *
     * <p>默认不支持，由 ReversiGame 覆写。</p>
     */
    public void pass() {
        throw new UnsupportedOperationException(GameTexts.unsupportedPassExceptionMessage());
    }

    /**
     * 获取当前轮到的玩家。
     *
     * @return 当前玩家
     */
    @Override
    public Piece getTurn() {
        return turn;
    }

    /**
     * 切换到下一位玩家。
     */
    public void switchTurn() {
        turn = turn.opponent();
    }

    /**
     * 获取黑方得分。
     *
     * @return 黑方得分
     */
    @Override
    public int getBlackScore() {
        return blackScore;
    }

    /**
     * 获取白方得分。
     *
     * @return 白方得分
     */
    @Override
    public int getWhiteScore() {
        return whiteScore;
    }

    /**
     * 重新统计黑白双方得分。
     */
    public void refreshScores() {
        blackScore = board.getCount(Piece.BLACK);
        whiteScore = board.getCount(Piece.WHITE);
    }

    /**
     * 获取当前棋盘对象。
     *
     * @return 黑白棋棋盘
     */
    @Override
    public Board getBoard() {
        return board;
    }

    /**
     * 获取棋盘格子的显示字符。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 格子显示字符
     */
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