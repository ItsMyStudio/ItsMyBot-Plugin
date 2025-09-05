package studio.itsmy.itsmybot.file;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * Abstract base class for plugin file implementations.
 * <p>
 * Provides common fields and accessors for the underlying file
 * and its loaded {@link FileConfiguration}.
 */
public abstract class BasePluginFile implements PluginFile {

    /** Reference to the main plugin instance. */
    protected final ItsMyBotPlugin plugin;

    /** The configuration object for this file. */
    protected FileConfiguration config;

    /** The underlying file on disk. */
    protected File file;

    /**
     * Creates a new {@code BasePluginFile}.
     *
     * @param plugin the main plugin instance
     */
    protected BasePluginFile(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile() {
        return file;
    }
}