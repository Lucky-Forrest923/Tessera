package com.gameplatform.controller;

import com.gameplatform.model.BaseGame;
import com.gameplatform.model.MultiBoardManager;
import com.gameplatform.renderer.GamePanelRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamePlatformController {
    private final MultiBoardManager manager;
    private final Map<BaseGame, GamePanelRenderer> rendererCache = new HashMap<>();

    public GamePlatformController(MultiBoardManager manager) {
        this.manager = manager;
    }

    public GamePanelRenderer getRenderer(BaseGame game) {
        return rendererCache.computeIfAbsent(game, BaseGame::createRenderer);
    }

    // === Queries ===

    public BaseGame getActiveGame() {
        return manager.getActive();
    }

    public List<BaseGame> getGames() {
        return manager.getGames();
    }

    public int getCurrentBoardNumber() {
        return manager.getCurrentBoardNumber();
    }

    public boolean isSupportedType(String type) {
        return manager.isSupportedType(type);
    }

    public Map<String, String> getAvailableGameTypes() {
        return manager.getAddableGameTypes();
    }

    // === Mutations ===

    public void createGame(String type, int size) {
        manager.add(type, size);
        manager.select(manager.getGames().size());
    }

    public void selectGame(int number) {
        if (number >= 1 && number <= manager.getGames().size()) {
            manager.select(number);
        }
    }

    public void pass() {
        manager.getActive().processCommand("pass");
    }

    public void removeGame(int number) {
        manager.remove(number);
    }

    /** Delegate a raw command string to the active game. Returns true if the game understood it. */
    public boolean processGameCommand(String cmd) {
        return manager.getActive().processCommand(cmd);
    }

    public void setStatus(String msg, boolean isError) {
        manager.getActive().setStatus(msg, isError);
    }

    public void handleCellClick(int row, int col, boolean isSecondary, boolean isFlagMode) {
        BaseGame game = getActiveGame();
        if (!game.isOver()) {
            game.onCellClick(row, col, isSecondary, isFlagMode);
        }
    }

    /** Process a command-line-style input and return the result. */
    public CommandResult dispatchCommand(String input) {
        if (input == null || input.isEmpty()) return CommandResult.NONE;

        if ("quit".equalsIgnoreCase(input)) return CommandResult.QUIT;

        if ("demo".equalsIgnoreCase(input)) {
            return CommandResult.DEMO;
        }

        if (isSupportedType(input)) {
            createGame(input.toLowerCase(), 8);
            return CommandResult.GAME_CREATED;
        }

        if (input.matches("\\d+") || input.matches("(?i)(s|switch)\\s+\\d+")) {
            String[] parts = input.split("\\s+");
            String numStr = parts.length > 1 ? parts[1] : parts[0];
            try {
                int num = Integer.parseInt(numStr);
                List<BaseGame> games = getGames();
                if (num >= 1 && num <= games.size()) {
                    selectGame(num);
                    return CommandResult.GAME_SWITCHED;
                }
                setStatus("Invalid game number: " + num + " (1-" + games.size() + ")", true);
                return CommandResult.REFRESH_STATUS;
            } catch (NumberFormatException e) {
                return CommandResult.NONE;
            }
        }

        if ("pass".equalsIgnoreCase(input)) {
            pass();
            return CommandResult.PASSED;
        }

        return processGameCommand(input) ? CommandResult.PROCESSED : CommandResult.NOT_PROCESSED;
    }

    public enum CommandResult {
        NONE, QUIT, DEMO, REFRESH_STATUS, GAME_CREATED, GAME_SWITCHED, PASSED, PROCESSED, NOT_PROCESSED
    }
}