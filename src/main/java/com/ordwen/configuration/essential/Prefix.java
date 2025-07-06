package com.ordwen.configuration.essential;

import com.ordwen.configuration.ConfigFactory;
import com.ordwen.configuration.IConfigurable;
import org.bukkit.configuration.file.FileConfiguration;

public class Prefix implements IConfigurable {

    private final FileConfiguration config;

    private String str;

    public Prefix(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public void load() {
        str = config.getString("prefix", "");
    }

    public String getPrefixInternal() {
        return str;
    }

    private static Prefix getInstance() {
        return ConfigFactory.getConfig(Prefix.class);
    }

    public static String getPrefix() {
        return getInstance().getPrefixInternal();
    }
}
