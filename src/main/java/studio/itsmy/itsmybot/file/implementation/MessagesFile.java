package studio.itsmy.itsmybot.file.implementation;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.file.BasePluginFile;
import studio.itsmy.itsmybot.util.PluginLogger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Manages the {@code messages.yml} file containing all translatable plugin messages.
 * <p>
 * This class ensures that missing messages are automatically added with their default
 * values (from {@link Messages}), and saves them back to disk when necessary.
 *
 * <p>Use {@link #getInstance()} to access the singleton instance at runtime.
 */
public class MessagesFile extends BasePluginFile {

    private static MessagesFile instance;

    /**
     * Creates a new {@code MessagesFile} manager.
     *
     * @param plugin the main plugin instance
     */
    public MessagesFile(ItsMyBotPlugin plugin) {
        super(plugin);
        instance = this;
    }

    /**
     * Loads (or reloads) the {@code messages.yml} file.
     * <p>
     * If the file does not exist, it is copied from the plugin's resources.
     * Missing message keys are automatically added with default values from {@link Messages}.
     */
    @Override
    public void load() {
        file = new File(plugin.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
            PluginLogger.info("Messages file created.");
        }

        config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (Exception e) {
            PluginLogger.error("An error occurred while loading the messages file.");
            PluginLogger.error(e.getMessage());
        }

        boolean missingMessages = false;
        for (Messages item : Messages.values()) {
            if (config.getString(item.getPath()) == null) {
                missingMessages = true;
                config.set(item.getPath(), item.getDefault());
            }
        }

        if (missingMessages) {
            try {
                config.save(file);
            } catch (Exception e) {
                PluginLogger.error("An error occurred while saving the messages file.");
                PluginLogger.error(e.getMessage());
            }

            PluginLogger.warn("Some messages are missing in the messages file. Default messages have been added.");
        }
    }

    /**
     * Retrieves a message string from the file, falling back to a default value if missing.
     *
     * @param path         the configuration path to the message
     * @param defaultValue the default value to return if missing
     * @return the message string (never null)
     */
    public String get(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    /**
     * Returns the singleton instance of {@code MessagesFile}.
     *
     * @return the instance, or {@code null} if {@link #load()} has not been called yet
     */
    public static MessagesFile getInstance() {
        return instance;
    }
}