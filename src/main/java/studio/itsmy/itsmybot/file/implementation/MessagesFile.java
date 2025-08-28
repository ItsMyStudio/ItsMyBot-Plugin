package studio.itsmy.itsmybot.file.implementation;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.file.BasePluginFile;
import studio.itsmy.itsmybot.util.PluginLogger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessagesFile extends BasePluginFile {

    private static MessagesFile instance;

    public MessagesFile(ItsMyBotPlugin plugin) {
        super(plugin);
        instance = this;
    }

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

    public String get(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public static MessagesFile getInstance() {
        return instance;
    }
}