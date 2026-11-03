package com.gameplatform.view;

import com.gameplatform.controller.GamePlatformController;
import com.gameplatform.model.*;
import com.gameplatform.renderer.CellClickContext;
import com.gameplatform.renderer.GamePanelRenderer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

public class JavaFXAPP extends Application {
    private static GamePlatformController STATIC_CONTROLLER;
    private GamePlatformController controller;

    // UI components
    private VBox boardPanel;
    private GridPane boardGrid;
    private VBox statusPanel;
    private VBox gameListPanel;
    private VBox gameListBox;
    private Label messageLabel;
    private Button passButton;
    private ToggleButton flagToggleButton;
    private Button demoButton;
    private TextField commandInput;
    private boolean demoPlaying;
    private Timeline demo;
    private int demoGameIndex = -1;

    private GamePanelRenderer getRenderer(BaseGame game) {
        return controller.getRenderer(game);
    }

    public static void setController(GamePlatformController controller) {
        STATIC_CONTROLLER = controller;
    }

    @Override
    public void start(Stage primaryStage) {
        this.controller = STATIC_CONTROLLER;
        if (controller == null) {
            throw new IllegalStateException("controller is null");
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2c3e50;");

        // Main content area: three columns with thin dividers
        boardPanel = createBoardPanel();
        statusPanel = createStatusPanel();
        gameListPanel = createGameListPanel();

        HBox mainContent = new HBox();
        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.setStyle("-fx-background-color: #2c3e50; -fx-pref-width: 2;");
        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep2.setStyle("-fx-background-color: #2c3e50; -fx-pref-width: 2;");

        boardPanel.setMinWidth(0);
        statusPanel.setMinWidth(0);
        gameListPanel.setMinWidth(0);
        HBox.setHgrow(boardPanel, Priority.ALWAYS);
        HBox.setHgrow(statusPanel, Priority.ALWAYS);
        HBox.setHgrow(gameListPanel, Priority.ALWAYS);

        mainContent.getChildren().addAll(boardPanel, sep1, statusPanel, sep2, gameListPanel);
        root.setCenter(mainContent);

        // Bottom area: controls + command input
        VBox bottomArea = new VBox(3);
        bottomArea.getChildren().addAll(createBottomControls(), createCommandInput());
        root.setBottom(bottomArea);

        Scene scene = new Scene(root, 1400, 900);
        primaryStage.setTitle("Game Platform - Lab 6");
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshAll();
    }

    // ========== Panel Creation ==========

    private VBox createBoardPanel() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #ecf0f1;");

        Label title = new Label("BOARD");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);

        box.getChildren().addAll(title, boardGrid);
        return box;
    }

    private VBox createStatusPanel() {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f5f6fa;");

        Label title = new Label("STATUS");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        panel.getChildren().add(title);

        return panel;
    }

    private VBox createGameListPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f5f6fa;");

        Label title = new Label("GAME LIST");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        gameListBox = new VBox(2);
        gameListBox.setPadding(new Insets(5, 0, 5, 0));

        // Create game buttons
        Label addTitle = new Label("Create New Game:");
        addTitle.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        HBox buttonRow = new HBox(6);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        for (Map.Entry<String, String> entry : controller.getAvailableGameTypes().entrySet()) {
            Button btn = new Button(entry.getValue());
            String type = entry.getKey();
            btn.setOnAction(e -> handleCreateGame(type));
            buttonRow.getChildren().add(btn);
        }

        panel.getChildren().addAll(title, gameListBox, addTitle, buttonRow);
        return panel;
    }

    private HBox createBottomControls() {
        HBox box = new HBox(12);
        box.setPadding(new Insets(10, 15, 10, 15));
        box.setStyle("-fx-background-color: #34495e;");
        box.setAlignment(Pos.CENTER_LEFT);

        Label cmdLabel = new Label("Controls:");
        cmdLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        passButton = new Button("  Pass  ");
        passButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        passButton.setOnAction(e -> handlePass());

        flagToggleButton = new ToggleButton("  Flag: OFF  ");
        flagToggleButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        flagToggleButton.setOnAction(e -> {
            if (flagToggleButton.isSelected()) {
                flagToggleButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
                flagToggleButton.setText("  Flag: ON  ");
            } else {
                flagToggleButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
                flagToggleButton.setText("  Flag: OFF  ");
            }
        });

        demoButton = new Button("  Demo  ");
        demoButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        demoButton.setOnAction(e -> handleDemo());

        Button quitButton = new Button("  Quit  ");
        quitButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        quitButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(cmdLabel, passButton, flagToggleButton, demoButton, spacer, quitButton);
        return box;
    }

    private HBox createCommandInput() {
        HBox box = new HBox(8);
        box.setPadding(new Insets(4, 15, 8, 15));
        box.setStyle("-fx-background-color: #2c3e50;");
        box.setAlignment(Pos.CENTER_LEFT);

        Label prompt = new Label("Command >");
        prompt.setStyle("-fx-text-fill: #ecf0f1; -fx-font-weight: bold; -fx-font-size: 13px;");

        commandInput = new TextField();
        commandInput.setPromptText("Enter command (e.g. a1, pass, s 2, demo, quit)");
        commandInput.setStyle("-fx-font-size: 13px; -fx-padding: 4 8;");
        commandInput.setPrefWidth(400);
        HBox.setHgrow(commandInput, Priority.ALWAYS);

        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");

        sendBtn.setOnAction(e -> processCommandInput());
        commandInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                processCommandInput();
            }
        });

        box.getChildren().addAll(prompt, commandInput, sendBtn);
        return box;
    }

    // ========== Command Processing ==========

    private void processCommandInput() {
        String input = commandInput.getText().trim();
        commandInput.clear();
        if (input.isEmpty()) return;

        switch (controller.dispatchCommand(input)) {
            case QUIT -> {
                Platform.exit();
                System.exit(0);
            }
            case DEMO -> handleDemo();
            case GAME_CREATED, GAME_SWITCHED -> {
                getRenderer(controller.getActiveGame()).clearSelection();
                refreshAll();
            }
            case PASSED, PROCESSED -> refreshAll();
            case REFRESH_STATUS, NOT_PROCESSED -> refreshStatus();
            case NONE -> {}
        }
    }

    // ========== Refresh Methods ==========

    private void refreshAll() {
        if (controller.getGames().isEmpty()) {
            boardGrid.getChildren().clear();
            refreshGameList();
            return;
        }
        refreshBoard();
        refreshStatus();
        refreshGameList();
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        BaseGame game = controller.getActiveGame();
        passButton.setVisible(game.supportsPass());
        flagToggleButton.setVisible(game.supportsFlag());
        if (!game.supportsFlag() && flagToggleButton.isSelected()) {
            flagToggleButton.setSelected(false);
            flagToggleButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
            flagToggleButton.setText("  Flag: OFF  ");
        }
        demoButton.setVisible(!game.getDemoSteps().isEmpty());
    }

    // ========== Board Rendering ==========

    private void refreshBoard() {
        boardGrid.getChildren().clear();
        BaseGame game = controller.getActiveGame();
        int size = game.getSize();

        // Coordinate headers
        for (int c = 0; c < size; c++) {
            StackPane header = makeHeaderCell(String.valueOf((char) ('a' + c)));
            boardGrid.add(header, c + 1, 0);
        }
        for (int r = 0; r < size; r++) {
            StackPane header = makeHeaderCell(String.valueOf(r + 1));
            boardGrid.add(header, 0, r + 1);
        }
        boardGrid.add(makeHeaderCell(""), 0, 0);

        // Legal move hints for Reversi
        List<Position> legalMoves = (game instanceof HintProvider hp && hp.showLegalMovesAsHints())
                ? hp.getLegalMoves() : List.of();

        GamePanelRenderer renderer = getRenderer(game);
        CellClickContext ctx = new CellClickContext(
                this::refreshAll, this::refreshBoard, this::refreshStatus,
                (msg, err) -> { controller.setStatus(msg, err); refreshStatus(); },
                flagToggleButton::isSelected,
                (row, col, isSec, isFlag) -> { controller.handleCellClick(row, col, isSec, isFlag); refreshAll(); }
        );

        // Board cells
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                boolean isHint = legalMoves.contains(new Position(r, c));
                StackPane cell = renderer.renderCell(game, r, c, isHint, ctx);
                boardGrid.add(cell, c + 1, r + 1);
            }
        }
    }

    private StackPane makeHeaderCell(String text) {
        StackPane cell = new StackPane();
        cell.setPrefSize(56, 30);
        cell.setMinSize(56, 30);
        Text t = new Text(text);
        t.setFont(Font.font(15));
        t.setFill(Color.web("#2c3e50"));
        cell.getChildren().add(t);
        cell.setStyle("-fx-background-color: #bdc3c7; -fx-border-color: #95a5a6; -fx-border-width: 0.5;");
        return cell;
    }

    // ========== Status Panel ==========

    private void refreshStatus() {
        statusPanel.getChildren().clear();
        BaseGame game = controller.getActiveGame();
        int currentNum = controller.getCurrentBoardNumber();

        Label title = new Label("STATUS");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        statusPanel.getChildren().add(title);

        // Info card
        VBox infoCard = new VBox(4);
        infoCard.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: white; -fx-background-radius: 4;");

        Label gameInfo = new Label("Game #" + currentNum + ": " + game.getDisplayName());
        gameInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        infoCard.getChildren().add(gameInfo);

        if (game instanceof TurnBasedGame tbg) {
            int current = tbg.getCurrentPlayerIndex();
            String playerName = tbg.getPlayerName(current);
            Label playerLabel = new Label("Current Player: " + playerName);
            playerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 13px;");
            infoCard.getChildren().add(playerLabel);
        }

        if (game.isOver()) {
            Label overLabel = new Label("GAME OVER");
            overLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-font-size: 16px;");
            infoCard.getChildren().add(overLabel);
        }

        statusPanel.getChildren().add(infoCard);

        // Extra status lines (scores, mine counts, etc.)
        List<String> extraLines = game.getExtraStatusLines();
        if (!extraLines.isEmpty()) {
            VBox extraBox = new VBox(3);
            extraBox.setStyle("-fx-padding: 8; -fx-background-color: white; -fx-background-radius: 4; -fx-border-color: #bdc3c7; -fx-border-width: 1;");
            Label extraTitle = new Label("Game Info:");
            extraTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            extraBox.getChildren().add(extraTitle);
            for (String line : extraLines) {
                Label l = new Label("  " + line);
                l.setStyle("-fx-font-size: 12px;");
                extraBox.getChildren().add(l);
            }
            statusPanel.getChildren().add(extraBox);
        }

        // Rule
        Label ruleLabel = new Label("Rule: " + getRenderer(game).getRuleText());
        ruleLabel.setStyle("-fx-font-size: 12px; -fx-padding: 8 0 0 0; -fx-text-fill: #7f8c8d;");
        ruleLabel.setWrapText(true);
        statusPanel.getChildren().add(ruleLabel);

        // Message area
        VBox msgBox = new VBox(3);
        msgBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1 0 0 0; -fx-padding: 8 0 0 0;");

        messageLabel = new Label(game.getStatusMessage());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        messageLabel.setTextFill(game.isStatusError() ? Color.RED : Color.GREEN);
        msgBox.getChildren().add(messageLabel);

        statusPanel.getChildren().add(msgBox);

        // Available commands
        VBox cmdBox = new VBox(2);
        cmdBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1 0 0 0; -fx-padding: 8 0 0 0;");
        Label cmdTitle = new Label("Controls:");
        cmdTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        cmdBox.getChildren().add(cmdTitle);

        // Game-specific commands
        for (String cmd : game.getAvailableCommands()) {
            Label l = new Label("  " + cmd);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");
            cmdBox.getChildren().add(l);
        }

        // Platform controls
        Label switchLabel = new Label("  Click game in list to switch");
        switchLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");
        cmdBox.getChildren().add(switchLabel);

        Label createLabel = new Label("  Click buttons to create new game");
        createLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");
        cmdBox.getChildren().add(createLabel);

        statusPanel.getChildren().add(cmdBox);
    }

    // ========== Game List ==========

    private void refreshGameList() {
        gameListBox.getChildren().clear();
        List<BaseGame> games = controller.getGames();
        int currentNum = controller.getCurrentBoardNumber();

        for (int i = 0; i < games.size(); i++) {
            BaseGame g = games.get(i);
            String status = g.isOver() ? "over" : "running";
            boolean isCurrent = (i + 1 == currentNum);
            String text = (isCurrent ? "\u25B6 " : "   ") + (i + 1) + ". " + g.getType() + " [" + status + "]";

            Label item = new Label(text);
            item.setStyle("-fx-font-family: monospace; -fx-font-size: 14px; -fx-padding: 4 8;"
                    + (isCurrent ? " -fx-background-color: #d5f5e3; -fx-font-weight: bold;" : ""));
            item.setMaxWidth(Double.MAX_VALUE);

            final int gameIndex = i;
            item.setOnMouseClicked(e -> handleSelectGame(gameIndex + 1));
            item.setOnMouseEntered(e -> {
                if (!isCurrent) item.setStyle("-fx-font-family: monospace; -fx-font-size: 14px; -fx-padding: 4 8; -fx-background-color: #ebedef;");
            });
            item.setOnMouseExited(e -> {
                if (!isCurrent) item.setStyle("-fx-font-family: monospace; -fx-font-size: 14px; -fx-padding: 4 8;");
            });

            gameListBox.getChildren().add(item);
        }
    }

    // ========== Event Handlers ==========

    private void handleSelectGame(int number) {
        stopDemo();
        controller.selectGame(number);
        getRenderer(controller.getActiveGame()).clearSelection();
        refreshAll();
    }

    private void handleCreateGame(String type) {
        stopDemo();
        controller.createGame(type, 8);
        getRenderer(controller.getActiveGame()).clearSelection();
        refreshAll();
        controller.setStatus("Created new " + type + " game", false);
        refreshStatus();
    }

    private void handlePass() {
        if (controller.getActiveGame().supportsPass()) {
            controller.pass();
            refreshAll();
        }
    }

    private void stopDemo() {
        if (demo != null) {
            demo.stop();
            demo = null;
        }
        if (demoGameIndex > 0) {
            int removeIdx = demoGameIndex;
            demoGameIndex = -1;
            controller.removeGame(removeIdx);
        }
        demoPlaying = false;
        demoButton.setDisable(false);
    }

    private void handleDemo() {
        if (demoPlaying) return;
        List<String> types = new java.util.ArrayList<>(controller.getAvailableGameTypes().keySet());
        if (types.isEmpty()) return;

        demoPlaying = true;
        demoButton.setDisable(true);

        int originalNum = controller.getCurrentBoardNumber();
        playNextDemo(types, 0, originalNum);
    }

    private void playNextDemo(List<String> types, int index, int originalNum) {
        if (index >= types.size()) {
            endDemo(originalNum);
            return;
        }

        String type = types.get(index);
        controller.createGame(type, 8);
        int demoNum = controller.getCurrentBoardNumber();
        demoGameIndex = demoNum;
        BaseGame demoGame = controller.getActiveGame();
        demoGame.prepareDemo();

        List<BaseGame.DemoStep> steps = demoGame.getDemoSteps();
        if (steps.isEmpty()) {
            controller.removeGame(demoNum);
            demoGameIndex = -1;
            playNextDemo(types, index + 1, originalNum);
            return;
        }

        controller.setStatus("Demo starting for " + demoGame.getDisplayName() + "...", false);
        refreshAll();

        Timeline timeline = new Timeline();
        demo = timeline;
        for (int i = 0; i < steps.size(); i++) {
            BaseGame.DemoStep step = steps.get(i);
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(demoGame.getDemoStepDelayMs() * (i + 1)),
                    e -> {
                        demoGame.processCommand(step.command());
                        controller.setStatus(step.explanation(), false);
                        refreshAll();
                    }
            ));
        }

        timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(demoGame.getDemoStepDelayMs() * (steps.size() + 1)),
                e -> {}
        ));

        timeline.setOnFinished(e -> {
            controller.removeGame(demoNum);
            demoGameIndex = -1;
            playNextDemo(types, index + 1, originalNum);
        });
        timeline.play();
    }

    private void endDemo(int originalNum) {
        controller.selectGame(originalNum);
        controller.getRenderer(controller.getActiveGame()).clearSelection();
        controller.setStatus("All demos finished", false);
        demoPlaying = false;
        demo = null;
        demoButton.setDisable(false);
        refreshAll();
    }
}