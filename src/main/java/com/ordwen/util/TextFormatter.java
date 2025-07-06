package com.ordwen.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class TextFormatter {

    private TextFormatter() {
    }

    /**
     * Format a message, replacing color codes.
     *
     * @param message message to format
     */
    public static String format(String message) {
        if (message == null) return null;
        return replaceAll(message);
    }

    /**
     * Format a message for a player, replacing placeholders and color codes.
     *
     * @param player  player
     * @param message message to format
     * @return formatted message
     */
    public static String format(Player player, String message) {
        if (message == null) return null;

        if (PluginUtils.isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        message = replaceAll(message);

        return message;
    }

    /**
     * Apply color codes to a message.
     *
     * @param message message to apply color codes to
     * @return message with color codes applied
     */
    private static String replaceAll(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    // TO DO: MiniMessage support
}
