package studio.itsmy.itsmybot.file.implementation;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.file.BasePluginFile;
import studio.itsmy.itsmybot.util.PluginLogger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Manages the {@code config.yml} file containing all plugin configuration options.
 * <p>
 * This class ensures that the file exists and is properly loaded into memory.
 * Unlike {@link MessagesFile}, it does not automatically add missing values.
 */
public class ConfigurationFile extends BasePluginFile {

    /**
     * Creates a new {@code ConfigurationFile} manager.
     *
     * @param plugin the main plugin instance
     */
    public ConfigurationFile(ItsMyBotPlugin plugin) {
        super(plugin);
    }

    /**
     * Loads (or reloads) the {@code config.yml} file.
     * <p>
     * If the file does not exist, it is copied from the plugin's resources.
     */
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