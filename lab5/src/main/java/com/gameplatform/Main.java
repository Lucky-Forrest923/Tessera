package com.gameplatform;

import com.gameplatform.controller.GameController;
import com.gameplatform.model.MultiBoardManager;
import com.gameplatform.plugin.GamePlugin;
import com.gameplatform.plugin.GamePluginRegistry;
import com.gameplatform.view.TerminalUI;

import java.util.ServiceLoader;

public class Main {
    public static void main(String[] args) {
        try (TerminalUI ui = new TerminalUI()) {
            GamePluginRegistry registry = new GamePluginRegistry();
            // 使用 SPI 自动加载所有 GamePlugin 实现
            ServiceLoader<GamePlugin> loader = ServiceLoader.load(GamePlugin.class);
            for (GamePlugin plugin : loader) {
                registry.register(plugin);
            }
            MultiBoardManager manager = new MultiBoardManager(registry, 8);
            new GameController(ui, manager).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}