package studio.itsmy.itsmybot.file;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.file.implementation.ConfigurationFile;
import studio.itsmy.itsmybot.file.implementation.MessagesFile;

public class FilesManager {

    private final ItsMyBotPlugin plugin;

    private final ConfigurationFile configurationFile;

    public FilesManager(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
        this.configurationFile = new ConfigurationFile(plugin);
    }

    /**
     * Load all files.
     */
    public void load() {
        configurationFile.load();
        new MessagesFile(plugin).load();
    }

    public ConfigurationFile getConfigurationFile() {
        return configurationFile;
    }
}
