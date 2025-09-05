package studio.itsmy.itsmybot.configuration;

import studio.itsmy.itsmybot.configuration.essential.Prefix;
import studio.itsmy.itsmybot.configuration.essential.WSConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central configuration factory that manages the registration and retrieval of
 * {@link IConfigurable} configuration objects.
 * <p>
 * This class is designed as a static utility holder and cannot be instantiated.
 * It allows you to register all configuration modules from a {@link FileConfiguration}
 * and later retrieve them by their class.
 */
public class ConfigFactory {

    /** Private constructor to prevent instantiation. */
    private ConfigFactory() {}

    /**
     * Holds all registered configuration modules mapped by their class.
     */
    private static final Map<Class<? extends IConfigurable>, IConfigurable> configs = new LinkedHashMap<>();

    /**
     * Registers and loads all configuration modules.
     *
     * @param config the {@link FileConfiguration} from which to load data
     */
    public static void registerConfigs(FileConfiguration config) {
        configs.clear();

        configs.put(WSConfig.class, new WSConfig(config));
        configs.put(Prefix.class, new Prefix(config));

        configs.values().forEach(IConfigurable::load);
    }

    /**
     * Retrieves a configuration instance of the given type.
     *
     * @param clazz the class of the configuration to retrieve
     * @param <T>   the type of configuration
     * @return the loaded configuration instance, or {@code null} if not found
     */
    public static <T extends IConfigurable> T getConfig(Class<T> clazz) {
        return clazz.cast(configs.get(clazz));
    }
}
