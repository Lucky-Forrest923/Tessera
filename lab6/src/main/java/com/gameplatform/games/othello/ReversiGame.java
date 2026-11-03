package com.gameplatform.games.othello;

import com.gameplatform.model.HintProvider;
import com.gameplatform.model.Position;
import com.gameplatform.renderer.GamePanelRenderer;

import java.util.ArrayList;
import java.util.List;

public class ReversiGame extends OthelloGame implements HintProvider {
    private static final int[][] DIRS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1}, {1, 0}, {1, 1}
    };

    public ReversiGame(int size) {
        super(size);
    }

    @Override
    public String getType() {
        return "reversi";
    }

    @Override
    public String getDisplayName() {
        return "黑白棋";
    }

    @Override
    public GamePanelRenderer createRenderer() {
        return new OthelloStyleRenderer(
                "Flip opponent's pieces by sandwiching. Pass if no legal moves");
    }

    @Override
    public boolean supportsPass() { return true; }

    @Override
    public boolean processCommand(String cmd) {
        if (cmd.equalsIgnoreCase("pass")) {
            pass();
            return true;
        }
        return super.processCommand(cmd);
    }

    @Override
    public List<String> getAvailableCommands() {
        return List.of("- 落子：a1", "- 跳过(pass)：pass");
    }

    @Override
    public boolean showLegalMovesAsHints() {
        return !isOver();
    }

    @Override
    protected boolean doPlay(Position position) {
        List<Position> flips = getFlips(position.row(), position.col(), getTurn());
        if (flips.isEmpty()) {
            setStatus(OthelloTexts.INVALID_MOVE, true);
            return false;
        }

        board.setPiece(position.row(), position.col(), getTurn());
        for (Position p : flips) {
            board.setPiece(p.row(), p.col(), getTurn());
        }

        switchTurn();
        NextState state = checkGameStatus();
        if (state == NextState.HAS_MOVES) {
            setStatus(OthelloTexts.MOVE_SUCCESS_EXCLAIM, false);
        } else if (state == NextState.NEED_PASS) {
            setStatus(OthelloTexts.PASS_PROMPT, true);
        }
        return true;
    }

    private void pass() {
        if (isOver()) {
            setStatus(OthelloTexts.GAME_OVER, true);
            return;
        }
        if (!getLegalMoves().isEmpty()) {
            setStatus(OthelloTexts.PASS_NOT_ALLOWED, true);
            return;
        }
        switchTurn();
        refreshScores();
        NextState state = checkGameStatus();
        if (state == NextState.HAS_MOVES) {
            setStatus(OthelloTexts.PASS_SUCCESS, false);
        } else if (state == NextState.NEED_PASS) {
            setStatus(OthelloTexts.PASS_PROMPT, true);
        }
    }

    private NextState checkGameStatus() {
        if (board.isFull()) {
            setOver();
            setStatus(OthelloTexts.BOARD_FULL_OVER, false);
            return NextState.GAME_OVER;
        }
        if (!getLegalMoves().isEmpty()) {
            return NextState.HAS_MOVES;
        }
        switchTurn();
        boolean opponentHasMoves = !getLegalMoves().isEmpty();
        switchTurn();

        if (!opponentHasMoves) {
            setOver();
            setStatus(OthelloTexts.NO_MOVES_OVER, false);
            return NextState.GAME_OVER;
        }
        return NextState.NEED_PASS;
    }

    private enum NextState {
        GAME_OVER,
        HAS_MOVES,
        NEED_PASS
    }

    @Override
    public List<Position> getLegalMoves() {
        List<Position> moves = new ArrayList<>();
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                if (!board.isOccupied(r, c) && !getFlips(r, c, getTurn()).isEmpty()) {
                    moves.add(new Position(r, c));
                }
            }
        }
        return moves;
    }

    private List<Position> getFlips(int row, int col, Piece piece) {
        List<Position> flips = new ArrayList<>();
        for (int[] d : DIRS) {
            List<Position> candidates = new ArrayList<>();
            int r = row + d[0];
            int c = col + d[1];
            while (!board.isOutOfBounds(r, c) && board.getPiece(r, c) == piece.opponent()) {
                candidates.add(new Position(r, c));
                r += d[0];
                c += d[1];
            }
            if (!board.isOutOfBounds(r, c) && board.getPiece(r, c) == piece && !candidates.isEmpty()) {
                flips.addAll(candidates);
            }
        }
        return flips;
    }

    @Override
    public List<DemoStep> getDemoSteps() {
        return List.of(
                // ===== 基本翻转机制 =====
                new DemoStep("d3", "【翻转机制】黑方d3，夹吃翻转d4白棋——夹吃翻转规则"),
                new DemoStep("c3", "【回合交替】白方c3，翻转d4回白——轮流落子"),
                new DemoStep("b3", "【边路夹吃】黑方b3——利用边路进行多方向夹吃"),
                new DemoStep("b2", "白方b2——在边路展开"),
                // ===== 边角争夺 =====
                new DemoStep("b1", "黑方b1——抢占边角位置"),
                new DemoStep("a1", "【占角！】白方a1夺取角落——角落不会被翻转"),
                new DemoStep("c4", "黑方c4——重新争夺中央"),
                new DemoStep("c1", "白方c1——沿边路扩张"),
                new DemoStep("c2", "黑方c2——封堵白方"),
                new DemoStep("d2", "白方d2——向中心突破"),
                new DemoStep("d1", "黑方d1——继续边路压制"),
                new DemoStep("e1", "白方e1——争夺上边"),
                // ===== 空间压缩 =====
                new DemoStep("a2", "黑方a2——压缩白方角部空间"),
                new DemoStep("a3", "白方a3——巩固角部墙壁"),
                new DemoStep("f5", "黑方f5——开辟新战线"),
                new DemoStep("a4", "白方a4——完成角部封锁"),
                // ===== Pass 演示 =====
                new DemoStep("pass", "【Pass跳过！】黑方此时无任何合法落子点，必须pass跳过回合——pass规则"),
                new DemoStep("e2", "【回合恢复】白方e2——黑方pass后白方继续落子，游戏继续")
        );
    }

    @Override
    public int getDemoStepDelayMs() {
        return 1500;
    }
}