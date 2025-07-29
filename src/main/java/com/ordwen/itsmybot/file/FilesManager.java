package com.ordwen.itsmybot.file;

import com.ordwen.itsmybot.ItsMyBotPlugin;
import com.ordwen.itsmybot.file.implementation.ConfigurationFile;
import com.ordwen.itsmybot.file.implementation.MessagesFile;

public class FilesManager {

    private final ItsMyBotPlugin plugin;

    private final ConfigurationFile configurationFile;

    public FilesManager(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
        this.configurationFile = new ConfigurationFile(plugin);
    }

    /**
     * Load all files.
     */
    public void load() {
        configurationFile.load();
        new MessagesFile(plugin).load();
    }

    public ConfigurationFile getConfigurationFile() {
        return configurationFile;
    }
}
