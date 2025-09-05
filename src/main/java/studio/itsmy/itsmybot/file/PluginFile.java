package studio.itsmy.itsmybot.file;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * Represents a generic plugin-managed file.
 * <p>
 * This interface defines the contract for any file used by the plugin, such as
 * configuration files ({@code config.yml}, {@code messages.yml}, etc.).
 * Implementations should handle loading and exposing their contents.
 */
public interface PluginFile {

    /**
     * Loads or reloads the plugin file into memory.
     * <p>
     * Implementations should ensure that the file exists, load it into
     * a {@link FileConfiguration} object, and handle any missing default values.
     */
    void load();

    /**
     * Gets the loaded {@link FileConfiguration} of this file.
     *
     * @return the configuration object (never {@code null} after {@link #load()} is called)
     */
    FileConfiguration getConfig();

    /**
     * Gets the actual file on disk for this plugin file.
     *
     * @return the {@link File} pointing to the resource
     */
    File getFile();
}