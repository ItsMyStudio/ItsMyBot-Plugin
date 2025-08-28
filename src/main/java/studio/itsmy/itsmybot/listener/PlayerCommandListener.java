package studio.itsmy.itsmybot.listener;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandListener implements Listener {

    private final ItsMyBotPlugin plugin;

    public PlayerCommandListener(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        plugin.getLogService().logPlayerCommand(event.getPlayer(), command);
    }
}
