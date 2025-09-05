package studio.itsmy.itsmybot.listener;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener responsible for handling player quit events.
 * <p>
 * When a player leaves the server, this listener delegates the event to the
 * plugin's {@link studio.itsmy.itsmybot.service.LogService} to log the departure.
 */
public class PlayerQuitListener implements Listener {

    private final ItsMyBotPlugin plugin;

    /**
     * Creates a new {@code PlayerQuitListener}.
     *
     * @param plugin the main plugin instance
     */
    public PlayerQuitListener(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the {@link PlayerQuitEvent}.
     * <p>
     * Invoked automatically by Bukkit when a player disconnects from the server.
     * This method logs the player's departure.
     *
     * @param event the player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getLogService().logPlayerLeave(event.getPlayer());
    }
}
