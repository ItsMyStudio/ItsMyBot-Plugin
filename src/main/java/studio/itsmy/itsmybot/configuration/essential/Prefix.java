package studio.itsmy.itsmybot.configuration.essential;

import studio.itsmy.itsmybot.configuration.ConfigFactory;
import studio.itsmy.itsmybot.configuration.IConfigurable;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Handles the configurable chat prefix used throughout the plugin.
 *
 * <p>This class loads the prefix string from {@code config.yml}
 * under the key {@code prefix}. The value is stored in memory for fast access.
 */
public class Prefix implements IConfigurable {

    private final FileConfiguration config;
    private String str;

    /**
     * Creates a new {@code Prefix} loader.
     *
     * @param config the {@link FileConfiguration} to read from
     */
    public Prefix(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Loads the prefix string from the configuration file.
     */
    @Override
    public void load() {
        str = config.getString("prefix", "");
    }

    /**
     * Returns the prefix value loaded from config.
     *
     * @return the prefix string (may be empty, never null)
     */
    public String getPrefixInternal() {
        return str;
    }

    /**
     * Returns the singleton {@code Prefix} instance managed by {@link ConfigFactory}.
     */
    private static Prefix getInstance() {
        return ConfigFactory.getConfig(Prefix.class);
    }

    /**
     * Returns the globally configured chat prefix.
     *
     * @return the prefix string (never null)
     */
    public static String getPrefix() {
        return getInstance().getPrefixInternal();
    }
}