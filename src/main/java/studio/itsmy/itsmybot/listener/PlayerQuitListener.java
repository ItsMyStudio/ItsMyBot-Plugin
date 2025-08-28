package studio.itsmy.itsmybot.listener;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final ItsMyBotPlugin plugin;

    public PlayerQuitListener(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getLogService().logPlayerLeave(event.getPlayer());
    }
}
