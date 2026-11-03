package com.gameplatform.model;

import com.gameplatform.plugin.GamePlugin;
import com.gameplatform.plugin.GamePluginRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MultiBoardManager {
    private final List<BaseGame> games = new ArrayList<>();
    private final GamePluginRegistry registry;
    private int defaultSize;
    private int currentIndex = 0;
    private boolean defaultsLoaded = false;

    public MultiBoardManager(GamePluginRegistry registry, int size) {
        this.registry = registry;
        this.defaultSize = size;
    }

    private void loadDefaults() {
        if (defaultsLoaded || registry == null) return;
        for (String type : registry.listDefaultTypeIds()) {
            GamePlugin plugin = registry.get(type);
            if (plugin != null) {
                games.add(plugin.createGame(defaultSize));
            }
        }
        defaultsLoaded = true;
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

    public Map<String, String> getAddableGameTypes() {
        Map<String, String> result = new LinkedHashMap<>();
        if (registry == null) return result;
        for (String typeId : registry.listTypeIds()) {
            GamePlugin plugin = registry.get(typeId);
            if (plugin != null) {
                result.put(typeId, plugin.getDisplayName());
            }
        }
        return result;
    }

    /**
     * 获取当前正在查看的游戏。
     *
     * @return 当前活动游戏
     */
    public BaseGame getActive() {
        loadDefaults();
        if (games.isEmpty()) {
            throw new IllegalStateException("No games available");
        }
        return games.get(currentIndex);
    }

    /**
     * 切换到指定编号的游戏。
     *
     * @param number 目标游戏编号（从 1 开始）
     */
    public void select(int number) {
        loadDefaults();
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
        loadDefaults();
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

    public void resetToDefaults(int size) {
        games.clear();
        defaultSize = size;
        defaultsLoaded = false;
    }

    /**
     * 移除指定编号（1-based）的游戏。
     */
    public void remove(int number) {
        int idx = number - 1;
        if (idx >= 0 && idx < games.size()) {
            games.remove(idx);
            if (currentIndex >= idx && currentIndex > 0) {
                currentIndex--;
            }
        }
    }
}