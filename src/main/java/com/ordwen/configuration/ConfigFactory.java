package com.ordwen.configuration;

import com.ordwen.configuration.essentials.Database;
import com.ordwen.configuration.essentials.WebSocketClient;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigFactory {

    private ConfigFactory() {}

    private static final Map<Class<? extends IConfigurable>, IConfigurable> configs = new LinkedHashMap<>();

    public static void registerConfigs(FileConfiguration config) {
        configs.put(WebSocketClient.class, new WebSocketClient(config));
        configs.put(Database.class, new Database(config));

        configs.values().forEach(IConfigurable::load);
    }

    public static <T extends IConfigurable> T getConfig(Class<T> clazz) {
        return clazz.cast(configs.get(clazz));
    }
}
