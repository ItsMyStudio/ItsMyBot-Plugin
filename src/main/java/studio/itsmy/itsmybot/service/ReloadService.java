package studio.itsmy.itsmybot.service;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.configuration.ConfigFactory;
import studio.itsmy.itsmybot.file.FilesManager;
import studio.itsmy.itsmybot.util.PluginLogger;

public class ReloadService {

    private final ItsMyBotPlugin plugin;

    public ReloadService(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        final FilesManager filesManager = new FilesManager(plugin);
        try {
            filesManager.load();
            ConfigFactory.registerConfigs(filesManager.getConfigurationFile().getConfig());

            plugin.reloadWSClient();
        } catch (IllegalStateException e) {
            PluginLogger.error("An error occurred while reloading the plugin. Please check the logs for details.");
        }
    }
}
