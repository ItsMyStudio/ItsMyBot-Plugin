package studio.itsmy.itsmybot.listener;

import org.bukkit.OfflinePlayer;
import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.ws.handler.role.RoleSyncUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener responsible for handling player join events.
 * <p>
 * When a player joins the server, this listener:
 * <ul>
 *     <li>Triggers a full role synchronization via {@link RoleSyncUtil#sendFullRoleSync(ItsMyBotPlugin, OfflinePlayer)}</li>
 *     <li>Logs the player's connection in the plugin's {@link studio.itsmy.itsmybot.service.LogService}</li>
 * </ul>
 */
public class PlayerJoinListener implements Listener {

    private final ItsMyBotPlugin plugin;

    /**
     * Creates a new {@code PlayerJoinListener}.
     *
     * @param plugin the main plugin instance
     */
    public PlayerJoinListener(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the {@link PlayerJoinEvent}.
     * <p>
     * Invoked automatically by Bukkit when a player joins the server.
     * This method initiates a role synchronization and logs the event.
     *
     * @param event the player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        RoleSyncUtil.sendFullRoleSync(plugin, player);
        plugin.getLogService().logPlayerJoin(player);
    }
}
