package com.ordwen.configuration.essential;

import com.ordwen.configuration.ConfigFactory;
import com.ordwen.configuration.IConfigurable;
import com.ordwen.util.PluginLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class WSConfig implements IConfigurable {

    private final FileConfiguration config;

    private String host;
    private int port;
    private String serverId;
    private String jwtSecret;

    public WSConfig(FileConfiguration config) {
        this.config = config;
    }

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

    private static WSConfig getInstance() {
        return ConfigFactory.getConfig(WSConfig.class);
    }

    public static String getHost() {
        return getInstance().host;
    }

    public static int getPort() {
        return getInstance().port;
    }

    public static String getServerId() {
        return getInstance().serverId;
    }

    public static String getJwtSecret() {
        return getInstance().jwtSecret;
    }
}
