package com.ordwen.files.implementations;

import com.ordwen.ItsMyBotPlugin;
import com.ordwen.files.APluginFile;
import com.ordwen.utils.PluginLogger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigurationFile extends APluginFile {

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