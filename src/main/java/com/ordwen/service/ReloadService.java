package com.ordwen.service;

import com.ordwen.ItsMyBotPlugin;
import com.ordwen.configuration.ConfigFactory;
import com.ordwen.file.FilesManager;
import com.ordwen.util.PluginLogger;

public class ReloadService {

    private final ItsMyBotPlugin plugin;

    public ReloadService(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        final FilesManager filesManager = new FilesManager(plugin);
        try {
            filesManager.load();
            ConfigFactory.registerConfigs(filesManager.getConfigurationFile().getConfig());

            plugin.reloadWSClient();
        } catch (IllegalStateException e) {
            PluginLogger.error("An error occurred while reloading the plugin. Please check the logs for details.");
        }
    }
}
