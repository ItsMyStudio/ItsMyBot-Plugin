package com.ordwen.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class TextFormatter {

    private TextFormatter() {
    }

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().character('&').hexColors().build();

    private static BukkitAudiences audiences;

    public static void init(BukkitAudiences audiencesInstance) {
        audiences = audiencesInstance;
    }

    public static void send(Player player, String rawMessage) {
        if (rawMessage == null || audiences == null) return;

        final String withPlaceholders = PluginUtils.isPluginEnabled("PlaceholderAPI")
                ? PlaceholderAPI.setPlaceholders(player, rawMessage)
                : rawMessage;

        final Component component = MINI_MESSAGE.deserialize(withPlaceholders);
        audiences.player(player).sendMessage(component);
    }

    /**
     * Format a message for a player, replacing placeholders and color codes.
     *
     * @param player     player
     * @param rawMessage message to format
     * @return formatted message Component
     */
    public static Component format(Player player, String rawMessage) {
        if (rawMessage == null) return Component.empty();

        final String withPlaceholders = PluginUtils.isPluginEnabled("PlaceholderAPI")
                ? PlaceholderAPI.setPlaceholders(player, rawMessage)
                : rawMessage;

        return MINI_MESSAGE.deserialize(withPlaceholders);
    }

    public static String legacy(String message) {
        if (message == null) return null;

        final Component component = MiniMessage.miniMessage().deserialize(message);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
