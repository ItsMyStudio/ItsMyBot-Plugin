package studio.itsmy.itsmybot.util;

import org.bukkit.Bukkit;

/**
 * Utility class providing helper methods for plugin-related checks.
 * <p>
 * This class contains static methods to interact with the Bukkit
 * plugin manager and verify plugin state.
 */
public class PluginUtils {

    /** Private constructor to prevent instantiation. */
    private PluginUtils() {}

    /**
     * Checks whether a given plugin is currently enabled on the server.
     *
     * @param pluginName the name of the plugin to check
     * @return {@code true} if the plugin is enabled, {@code false} otherwise
     */
    public static boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }
}
