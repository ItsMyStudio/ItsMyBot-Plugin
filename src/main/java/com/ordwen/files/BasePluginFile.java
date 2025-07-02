package com.ordwen.files;

import com.ordwen.ItsMyBotPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public abstract class BasePluginFile implements PluginFile {

    protected final ItsMyBotPlugin plugin;

    protected FileConfiguration config;
    protected File file;

    protected BasePluginFile(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public File getFile() {
        return file;
    }
}