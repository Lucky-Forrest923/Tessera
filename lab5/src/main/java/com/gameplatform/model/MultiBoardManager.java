package com.gameplatform.model;

import com.gameplatform.plugin.GamePlugin;
import com.gameplatform.plugin.GamePluginRegistry;

import java.util.ArrayList;
import java.util.List;

public class MultiBoardManager {
    private final List<BaseGame> games = new ArrayList<>();
    private final GamePluginRegistry registry;
    private int currentIndex = 0;

    public MultiBoardManager(GamePluginRegistry registry, int size) {
        this.registry = registry;
        for (String type : registry.listDefaultTypeIds()) {
            GamePlugin plugin = registry.get(type);
            if (plugin != null) {
                games.add(plugin.createGame(size));
            }
        }
    }

    public void add(String type, int size) {
        if (registry == null || type == null) return;
        GamePlugin plugin = registry.get(type);
        if (plugin != null) {
            games.add(plugin.createGame(size));
        }
    }

    public boolean isSupportedType(String type) {
        return registry != null && registry.has(type);
    }

    public List<String> getAddableTypes() {
        return registry == null ? List.of() : registry.listTypeIds();
    }

    /**
     * 获取当前正在查看的游戏。
     *
     * @return 当前活动游戏
     */
    public BaseGame getActive() {
        return games.get(currentIndex);
    }

    /**
     * 切换到指定编号的游戏。
     *
     * @param number 目标游戏编号（从 1 开始）
     */
    public void select(int number) {
        if (number > 0 && number <= games.size()) {
            currentIndex = number - 1;
        }
    }

    /**
     * 获取当前所有游戏的只读视图。
     *
     * @return 游戏列表的只读副本
     */
    public List<BaseGame> getGames() {
        return List.copyOf(games);
    }

    /**
     * 获取当前游戏编号。
     *
     * @return 当前游戏编号（从 1 开始）
     */
    public int getCurrentBoardNumber() {
        return currentIndex + 1;
    }

    public boolean selectByType(String type) {
        if (type == null) return false;
        for (int i = 0; i < games.size(); i++) {
            if (type.equalsIgnoreCase(games.get(i).getType())) {
                currentIndex = i;
                return true;
            }
        }
        return false;
    }

    public void resetToDefaults(int size) {
        games.clear();
        if (registry != null) {
            for (String type : registry.listDefaultTypeIds()) {
                GamePlugin plugin = registry.get(type);
                if (plugin != null) {
                    games.add(plugin.createGame(size));
                }
            }
        }
        currentIndex = 0;
    }
}