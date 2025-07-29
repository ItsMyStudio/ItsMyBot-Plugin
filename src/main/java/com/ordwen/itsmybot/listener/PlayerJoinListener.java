package com.ordwen.itsmybot.listener;

import com.ordwen.itsmybot.ItsMyBotPlugin;
import com.ordwen.itsmybot.ws.handler.role.RoleSyncUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final ItsMyBotPlugin plugin;

    public PlayerJoinListener(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        RoleSyncUtil.sendFullRoleSync(plugin, player);
        plugin.getLogService().logPlayerJoin(player);
    }
}
