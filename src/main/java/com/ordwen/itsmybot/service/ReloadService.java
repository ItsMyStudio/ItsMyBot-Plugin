package com.ordwen.itsmybot.service;

import com.ordwen.itsmybot.ItsMyBotPlugin;
import com.ordwen.itsmybot.configuration.ConfigFactory;
import com.ordwen.itsmybot.file.FilesManager;
import com.ordwen.itsmybot.util.PluginLogger;

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
