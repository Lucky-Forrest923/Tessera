package com.gameplatform.plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GamePluginRegistry {
    private final Map<String, GamePlugin> plugins = new LinkedHashMap<>();

    public void register(GamePlugin plugin) {
        if (plugin == null || plugin.getTypeId() == null) return;
        plugins.put(plugin.getTypeId().toLowerCase(), plugin);
    }

    public boolean has(String typeId) {
        if (typeId == null) return false;
        return plugins.containsKey(typeId.toLowerCase());
    }

    public GamePlugin get(String typeId) {
        if (typeId == null) return null;
        return plugins.get(typeId.toLowerCase());
    }

    public List<String> listTypeIds() {
        return new ArrayList<>(plugins.keySet());
    }

    public List<String> listDefaultTypeIds() {
        List<String> defaults = new ArrayList<>();
        for (GamePlugin plugin : plugins.values()) {
            if (plugin.isDefaultGame()) {
                defaults.add(plugin.getTypeId());
            }
        }
        return defaults;
    }
}

