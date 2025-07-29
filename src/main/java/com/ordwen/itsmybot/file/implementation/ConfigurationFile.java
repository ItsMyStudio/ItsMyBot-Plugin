package com.ordwen.itsmybot.file.implementation;

import com.ordwen.itsmybot.ItsMyBotPlugin;
import com.ordwen.itsmybot.file.BasePluginFile;
import com.ordwen.itsmybot.util.PluginLogger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigurationFile extends BasePluginFile {

    public ConfigurationFile(ItsMyBotPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
            PluginLogger.info("Configuration file created.");
        }

        config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (Exception e) {
            PluginLogger.error("An error occurred while loading the configuration file.");
            PluginLogger.error(e.getMessage());
        }
    }
}