package studio.itsmy.itsmybot.file;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.file.implementation.ConfigurationFile;
import studio.itsmy.itsmybot.file.implementation.MessagesFile;

/**
 * Central manager for all plugin-managed files.
 * <p>
 * This class provides a single entry point to load and access configuration files
 * like {@code config.yml} and {@code messages.yml}.
 */
public class FilesManager {

    private final ItsMyBotPlugin plugin;
    private final ConfigurationFile configurationFile;

    /**
     * Creates a new {@code FilesManager} for the given plugin instance.
     *
     * @param plugin the main plugin instance
     */
    public FilesManager(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
        this.configurationFile = new ConfigurationFile(plugin);
    }

    /**
     * Loads all plugin files into memory.
     * <p>
     * This will create missing files from resources, load their contents,
     * and populate defaults when needed.
     */
    public void load() {
        configurationFile.load();
        new MessagesFile(plugin).load();
    }

    /**
     * Returns the {@link ConfigurationFile} instance.
     *
     * @return the loaded configuration file
     */
    public ConfigurationFile getConfigurationFile() {
        return configurationFile;
    }
}