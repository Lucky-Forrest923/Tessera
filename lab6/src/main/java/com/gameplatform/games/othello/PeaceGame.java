package com.gameplatform.games.othello;

import com.gameplatform.model.Position;
import com.gameplatform.renderer.GamePanelRenderer;

import java.util.ArrayList;
import java.util.List;

public class PeaceGame extends OthelloGame {

    public PeaceGame(int size) {
        super(size);
    }

    @Override
    public String getType() {
        return "peace";
    }

    @Override
    public String getDisplayName() {
        return "和平棋";
    }

    @Override
    public GamePanelRenderer createRenderer() {
        return new OthelloStyleRenderer(
                "Place pieces on empty cells, no flipping, board-fill ends game");
    }

    @Override
    public List<String> getAvailableCommands() {
        return List.of("- 落子：a1");
    }

    @Override
    protected boolean usesScores() {
        return false;
    }

    @Override
    protected boolean doPlay(Position position) {
        board.setPiece(position.row(), position.col(), getTurn());
        switchTurn();
        if (board.isFull()) {
            setOver();
            setStatus(OthelloTexts.BOARD_FULL_OVER, false);
        } else {
            setStatus(OthelloTexts.MOVE_SUCCESS, false);
        }
        return true;
    }


    @Override
    public List<DemoStep> getDemoSteps() {
        // 展示少量落子演示轮流机制，不填满整个棋盘以节省时间
        List<DemoStep> steps = new ArrayList<>();
        int[][] positions = {
                {0, 0}, {7, 7}, {0, 7}, {7, 0},  // 四角
                {3, 5}, {4, 2}, {2, 6}, {5, 1},  // 中间区域
                {1, 1}, {6, 6}                    // 近角
        };
        for (int[] position : positions) {
            int r = position[0], c = position[1];
            char colChar = (char) ('a' + c);
            String cmd = "" + colChar + (r + 1);
            String explanation = String.format("轮流落子：%s", cmd);
            steps.add(new DemoStep(cmd, explanation));
        }
        return steps;
    }

    @Override
    public int getDemoStepDelayMs() {
        return 800;
    }
}