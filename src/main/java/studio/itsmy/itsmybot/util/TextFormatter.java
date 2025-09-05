package studio.itsmy.itsmybot.util;

import studio.itsmy.itsmybot.configuration.essential.Prefix;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Utility class for sending and formatting text messages to players.
 * <p>
 * Uses Adventure's {@link MiniMessage} for rich text formatting and
 * automatically integrates with PlaceholderAPI if present.
 *
 * <p>Supports dynamic prefix replacement by substituting occurrences of
 * {@code <prefix>} with the value from {@link Prefix#getPrefix()}.
 */
public class TextFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static BukkitAudiences audiences;

    /** Private constructor to prevent instantiation. */
    private TextFormatter() {}

    /**
     * Initializes the {@link BukkitAudiences} instance used for sending messages.
     * <p>
     * This must be called during plugin enable before using {@link #send(Player, String)}.
     *
     * @param audiencesInstance the {@link BukkitAudiences} instance provided by the plugin
     */
    public static void init(BukkitAudiences audiencesInstance) {
        audiences = audiencesInstance;
    }

    /**
     * Sends a formatted message to a player.
     * <p>
     * The message is processed as follows:
     * <ul>
     *   <li>Replaces {@code <prefix>} with the plugin prefix</li>
     *   <li>Applies PlaceholderAPI placeholders if available</li>
     *   <li>Parses MiniMessage formatting tags</li>
     * </ul>
     *
     * @param player     the recipient player
     * @param rawMessage the raw MiniMessage string to send (may include placeholders and prefix)
     */
    public static void send(Player player, String rawMessage) {
        if (rawMessage == null || audiences == null) return;

        final String withPrefix = rawMessage.replace("<prefix>", Prefix.getPrefix());

        final String withPlaceholders = PluginUtils.isPluginEnabled("PlaceholderAPI")
                ? PlaceholderAPI.setPlaceholders(player, withPrefix)
                : withPrefix;

        final Component component = MINI_MESSAGE.deserialize(withPlaceholders);
        audiences.player(player).sendMessage(component);
    }

    /**
     * Formats a message for a player and returns it as a {@link Component}.
     * <p>
     * Unlike {@link #send(Player, String)}, this method does not send the message,
     * it only performs placeholder substitution and MiniMessage deserialization.
     *
     * @param player     the player used for placeholder replacement
     * @param rawMessage the raw MiniMessage string to format
     * @return the formatted {@link Component} (or {@link Component#empty()} if message is null)
     */
    public static Component format(Player player, String rawMessage) {
        if (rawMessage == null) return Component.empty();

        final String withPlaceholders = PluginUtils.isPluginEnabled("PlaceholderAPI")
                ? PlaceholderAPI.setPlaceholders(player, rawMessage)
                : rawMessage;

        return MINI_MESSAGE.deserialize(withPlaceholders);
    }

    /**
     * Converts a MiniMessage string into a legacy (section-symbol) colored string.
     * <p>
     * Useful for sending formatted messages to the console or plugins that
     * do not support Adventure components.
     *
     * @param message the MiniMessage string to convert
     * @return the legacy formatted string (or {@code null} if message is null)
     */
    public static String legacy(String message) {
        if (message == null) return null;

        final Component component = MiniMessage.miniMessage().deserialize(message);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
