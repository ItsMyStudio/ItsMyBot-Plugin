package com.ordwen.files;

import com.ordwen.ItsMyBotPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public abstract class APluginFile implements IPluginFile {

    protected final ItsMyBotPlugin plugin;

    protected FileConfiguration config;
    protected File file;

    protected APluginFile(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public File getFile() {
        return file;
    }
}