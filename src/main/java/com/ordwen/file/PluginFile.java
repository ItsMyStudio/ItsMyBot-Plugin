package com.ordwen.file;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public interface PluginFile {

    /**
     * Load the plugin file.
     */
    void load();

    /**
     * Get the configuration of the plugin file.
     *
     * @return the configuration
     */
    FileConfiguration getConfig();

    /**
     * Get the file of the plugin file.
     *
     * @return the file
     */
    File getFile();
}
