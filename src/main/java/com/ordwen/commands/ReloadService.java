package com.ordwen.commands;

import com.ordwen.ItsMyBotPlugin;
import com.ordwen.configuration.ConfigFactory;
import com.ordwen.utils.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ReloadService {

    private final ItsMyBotPlugin plugin;

    public ReloadService(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        try {
            plugin.reloadConfig();
            ConfigFactory.registerConfigs(plugin.getConfig());
        } catch (IllegalStateException e) {
            PluginLogger.error("An error occurred while reloading the plugin. Please check the logs for details.");
        }
    }
}
