package studio.itsmy.itsmybot.configuration.essential;

import studio.itsmy.itsmybot.configuration.ConfigFactory;
import studio.itsmy.itsmybot.configuration.IConfigurable;
import studio.itsmy.itsmybot.util.PluginLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Handles the WebSocket connection configuration for the plugin.
 *
 * <p>This class loads all critical WebSocket settings from {@code config.yml}
 * under the section {@code websocket}. If required values are missing, an exception
 * is thrown and the plugin should fail to enable.
 */
public class WSConfig implements IConfigurable {

    private final FileConfiguration config;

    private String host;
    private int port;
    private String serverId;
    private String jwtSecret;

    /**
     * Creates a new {@code WSConfig} loader.
     *
     * @param config the {@link FileConfiguration} to read from
     */
    public WSConfig(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Loads WebSocket configuration values from the {@code websocket} section.
     *
     * @throws IllegalArgumentException if required values are missing
     */
    @Override
    public void load() {
        final ConfigurationSection section = config.getConfigurationSection("websocket");
        if (section == null) {
            PluginLogger.error("websocket section is missing in config.yml");
            throw new IllegalArgumentException("Missing 'websocket' section");
        }

        host = section.getString("host", "localhost");
        port = section.getInt("port", 8765);
        serverId = section.getString("server_id");
        jwtSecret = section.getString("jwt_secret");

        if (serverId == null || jwtSecret == null) {
            PluginLogger.error("Missing 'server_id' or 'jwt_secret' in websocket config");
            throw new IllegalArgumentException("Missing critical websocket configuration values");
        }
    }

    /**
     * Returns the singleton {@code WSConfig} instance managed by {@link ConfigFactory}.
     */
    private static WSConfig getInstance() {
        return ConfigFactory.getConfig(WSConfig.class);
    }

    /**
     * Gets the WebSocket host.
     *
     * @return the host (default: {@code localhost})
     */
    public static String getHost() {
        return getInstance().host;
    }

    /**
     * Gets the WebSocket port.
     *
     * @return the port (default: {@code 8765})
     */
    public static int getPort() {
        return getInstance().port;
    }

    /**
     * Gets the server identifier used to authenticate this plugin instance.
     *
     * @return the server ID (never null)
     */
    public static String getServerId() {
        return getInstance().serverId;
    }

    /**
     * Gets the JWT secret used for signing authentication tokens.
     *
     * @return the secret key (never null)
     */
    public static String getJwtSecret() {
        return getInstance().jwtSecret;
    }
}
