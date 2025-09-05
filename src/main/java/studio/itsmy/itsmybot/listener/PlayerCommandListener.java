package studio.itsmy.itsmybot.listener;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Listener responsible for intercepting and logging player command executions.
 * <p>
 * This allows the plugin to track every command a player runs, before
 * Bukkit processes it.
 */
public class PlayerCommandListener implements Listener {

    private final ItsMyBotPlugin plugin;

    /**
     * Creates a new {@code PlayerCommandListener}.
     *
     * @param plugin the main plugin instance
     */
    public PlayerCommandListener(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the {@link PlayerCommandPreprocessEvent}.
     * <p>
     * Invoked automatically by Bukkit when a player executes a command.
     * This method extracts the raw command string (without the leading slash)
     * and logs it using the plugin's {@link studio.itsmy.itsmybot.service.LogService}.
     *
     * @param event the player command preprocess event
     */
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        plugin.getLogService().logPlayerCommand(event.getPlayer(), command);
    }
}
