package studio.itsmy.itsmybot.configuration;

/**
 * Represents a configurable module that can load its data from a source,
 * usually a {@link org.bukkit.configuration.file.FileConfiguration}.
 *
 * <p>All configuration modules must implement this interface
 * so that they can be managed by {@link ConfigFactory}.
 */
public interface IConfigurable {

    /**
     * Loads the configuration data.
     * <p>
     * Implementations should read from their underlying configuration source
     * and store the values in memory.
     */
    void load();
}