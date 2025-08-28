package studio.itsmy.itsmybot.util;

import org.bukkit.Bukkit;

public class PluginUtils {

    private PluginUtils() {}

    public static boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }
}
