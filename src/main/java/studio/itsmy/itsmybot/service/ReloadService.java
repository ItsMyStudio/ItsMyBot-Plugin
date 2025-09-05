package studio.itsmy.itsmybot.service;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.configuration.ConfigFactory;
import studio.itsmy.itsmybot.file.FilesManager;
import studio.itsmy.itsmybot.util.PluginLogger;

/**
 * Service responsible for reloading the plugin configuration and WebSocket client.
 * <p>
 * This class encapsulates the reload process:
 * <ul>
 *     <li>Reloads configuration files ({@code config.yml}, {@code messages.yml})</li>
 *     <li>Re-registers configuration values in {@link ConfigFactory}</li>
 *     <li>Reconnects the WebSocket client</li>
 * </ul>
 */
public class ReloadService {

    private final ItsMyBotPlugin plugin;

    /**
     * Creates a new {@code ReloadService}.
     *
     * @param plugin the main plugin instance
     */
    public ReloadService(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads all plugin resources.
     * <p>
     * This method reloads files via {@link FilesManager},
     * re-registers configuration classes with {@link ConfigFactory},
     * and attempts to reconnect the WebSocket client.
     *
     * <p>If an {@link IllegalStateException} is thrown during the process,
     * a generic error is logged to the console.
     */
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
