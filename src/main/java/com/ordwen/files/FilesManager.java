package com.ordwen.files;

import com.ordwen.ItsMyBotPlugin;
import com.ordwen.files.implementations.ConfigurationFile;
import com.ordwen.files.implementations.MessagesFile;

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
