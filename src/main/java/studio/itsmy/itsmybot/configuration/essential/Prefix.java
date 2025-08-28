package studio.itsmy.itsmybot.configuration.essential;

import studio.itsmy.itsmybot.configuration.ConfigFactory;
import studio.itsmy.itsmybot.configuration.IConfigurable;
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
