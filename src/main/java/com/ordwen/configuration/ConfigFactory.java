package com.ordwen.configuration;

import com.ordwen.configuration.essential.Prefix;
import com.ordwen.configuration.essential.WSConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigFactory {

    private ConfigFactory() {}

    private static final Map<Class<? extends IConfigurable>, IConfigurable> configs = new LinkedHashMap<>();

    public static void registerConfigs(FileConfiguration config) {
        configs.clear();

        configs.put(WSConfig.class, new WSConfig(config));
        configs.put(Prefix.class, new Prefix(config));

        configs.values().forEach(IConfigurable::load);
    }

    public static <T extends IConfigurable> T getConfig(Class<T> clazz) {
        return clazz.cast(configs.get(clazz));
    }
}
