package com.gameplatform;

import com.gameplatform.controller.GamePlatformController;
import com.gameplatform.model.MultiBoardManager;
import com.gameplatform.plugin.GamePlugin;
import com.gameplatform.plugin.GamePluginRegistry;
import com.gameplatform.view.JavaFXAPP;

import java.util.ServiceLoader;

public class Main {
    public static void main(String[] args) {
        GamePluginRegistry registry = new GamePluginRegistry();
        ServiceLoader<GamePlugin> loader = ServiceLoader.load(GamePlugin.class);
        for (GamePlugin plugin : loader) {
            registry.register(plugin);
        }
        MultiBoardManager manager = new MultiBoardManager(registry, 8);
        GamePlatformController controller = new GamePlatformController(manager);
        JavaFXAPP.setController(controller);
        javafx.application.Application.launch(JavaFXAPP.class, args);
    }
}