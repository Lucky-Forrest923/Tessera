package com.othello.controller;

import com.othello.io.GameIO;
import com.othello.io.GameViewData;
import com.othello.io.InputKey;
import com.othello.io.InputKeyType;
import com.othello.model.GameState;
import com.othello.model.MultiBoardManager;
import com.othello.model.Position;
import com.othello.model.ReversiGame;
import com.othello.view.TerminalUI;

/**
 * 控制台控制器。
 * 负责输入处理、流程流转与调用游戏规则对象。
 */
public class GameController implements AutoCloseable {
    private static final int INITIAL_BOARD_COUNT = 3;
    private static final int MAX_INPUT_LENGTH = 16;

    private final GameIO ui;
    private final InputParser inputParser = new InputParser();
    private final MessageFormatter messageFormatter = new MessageFormatter();
    private final StringBuilder inputBuffer = new StringBuilder();

    private MultiBoardManager boards;
    private Integer selectedBoardSize;
    private String setupMessage = "";
    private String transientMessage = "";
    private boolean transientError;

    /**
     * 构造函数。
     * @throws Exception 终端初始化异常
     */
    public GameController() throws Exception {
        this(new TerminalUI());
    }

    /**
     * 构造函数。
     * @param ui 输入输出实现
     */
    public GameController(GameIO ui) {
        this.ui = ui;
    }

    /**
     * 运行游戏主流程。
     * @throws Exception 读写终端异常
     */
    public void run() throws Exception {
        setupLoop();
        if (selectedBoardSize == null) {
            return;
        }

        boards = new MultiBoardManager(INITIAL_BOARD_COUNT, selectedBoardSize);
        inputBuffer.setLength(0);
        gameLoop();
    }

    private void setupLoop() throws Exception {
        while (selectedBoardSize == null) {
            ui.drawSetupScreen(inputBuffer.toString(), setupMessage);
            InputKey key = ui.readInput();

            if (key.type() == InputKeyType.ENTER) {
                String command = inputBuffer.toString().trim();
                inputBuffer.setLength(0);

                if (inputParser.isQuitCommand(command)) {
                    return;
                }

                Integer size = inputParser.parseBoardSize(command);
                if (size == null) {
                    setupMessage = "请输入 4 到 16 之间的偶数。";
                    continue;
                }

                selectedBoardSize = size;
                setupMessage = "";
                return;
            }

            if (key.type() == InputKeyType.BACKSPACE && inputBuffer.length() > 0) {
                inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                continue;
            }

            if (key.type() == InputKeyType.CHARACTER && key.character() != null && inputBuffer.length() < 3) {
                inputBuffer.append(key.character());
            }
        }
    }

    private void gameLoop() throws Exception {
        while (true) {
            ReversiGame game = boards.getActiveGame();
            game.updateTurnAndGameOver();
            GameState state = game.getState();

            // 使用 MessageFormatter 格式化消息
            String statusMessage = formatCurrentMessage(state);
            boolean error = isCurrentMessageError(state);

            String currentPlayer = state.isGameOver()
                    ? "游戏结束"
                    : messageFormatter.getPlayerName(state.getCurrentTurn());

            ui.drawGame(new GameViewData(
                    game.getBoard(),
                    currentPlayer,
                    inputBuffer.toString(),
                    statusMessage,
                    state.isGameOver(),
                    error,
                    state.getBlackCount(),
                    state.getWhiteCount(),
                    boards.getActiveBoardNumber(),
                    boards.getBoardCount()
            ));

            InputKey key = ui.readInput();
            if (key.type() == InputKeyType.ENTER) {
                String command = inputBuffer.toString().trim();
                inputBuffer.setLength(0);
                clearTransientMessage();

                if (inputParser.isQuitCommand(command)) {
                    return;
                }

                Integer boardNumber = inputParser.parseBoardNumber(command, boards.getBoardCount());
                if (boardNumber != null) {
                    boards.selectBoard(boardNumber);
                    setTransientMessage("已切换到棋盘 " + boardNumber, false);
                    continue;
                }

                handleMoveCommand(command);
                continue;
            }

            if (key.type() == InputKeyType.BACKSPACE && inputBuffer.length() > 0) {
                inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                continue;
            }

            if (key.type() == InputKeyType.CHARACTER && key.character() != null && inputBuffer.length() < MAX_INPUT_LENGTH) {
                inputBuffer.append(key.character());
            }
        }
    }

    /**
     * 处理移动命令。
     * @param command 用户输入的命令
     */
    private void handleMoveCommand(String command) {
        ReversiGame game = boards.getActiveGame();
        GameState state = game.getState();

        if (state.isGameOver()) {
            setTransientMessage(messageFormatter.formatStatusMessage(state,
                    state.getBlackCount(), state.getWhiteCount()), false);
            return;
        }

        Position position = inputParser.parsePosition(command);
        game.play(position);

        GameState updated = game.getState();
        if (updated.isError()) {
            setTransientMessage(messageFormatter.formatStatusMessage(updated,
                    updated.getBlackCount(), updated.getWhiteCount()), true);
            return;
        }

        if (updated.isGameOver()) {
            setTransientMessage(messageFormatter.formatStatusMessage(updated,
                    updated.getBlackCount(), updated.getWhiteCount()), false);
            return;
        }

        if (updated.getStatusCode() == com.othello.model.GameStatusCode.PASS) {
            setTransientMessage(messageFormatter.formatStatusMessage(updated,
                    updated.getBlackCount(), updated.getWhiteCount()), false);
            return;
        }

        setTransientMessage("落子成功。", false);
    }

    /**
     * 格式化当前消息（优先使用临时消息）。
     * @param state 游戏状态
     * @return 格式化后的消息
     */
    private String formatCurrentMessage(GameState state) {
        if (transientMessage != null && !transientMessage.isEmpty()) {
            return transientMessage;
        }
        return messageFormatter.formatStatusMessage(state,
                state.getBlackCount(), state.getWhiteCount());
    }

    /**
     * 判断当前消息是否为错误（优先使用临时错误标记）。
     * @param state 游戏状态
     * @return 是否为错误消息
     */
    private boolean isCurrentMessageError(GameState state) {
        if (transientMessage != null && !transientMessage.isEmpty()) {
            return transientError;
        }
        return state.isError();
    }

    /**
     * 设置临时消息。
     * @param message 消息内容
     * @param isError 是否为错误消息
     */
    private void setTransientMessage(String message, boolean isError) {
        this.transientMessage = message;
        this.transientError = isError;
    }

    /**
     * 清除临时消息。
     */
    private void clearTransientMessage() {
        this.transientMessage = "";
        this.transientError = false;
    }

    /**
     * 释放终端资源。
     * @throws Exception 关闭异常
     */
    @Override
    public void close() throws Exception {
        ui.close();
    }
}