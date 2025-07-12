package com.ordwen.listener;

import com.ordwen.ItsMyBotPlugin;
import com.ordwen.util.RoleSyncUtil;
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

        if (!RoleSyncUtil.sendFullRoleSync(plugin, player)) return;
        plugin.getLogService().logPlayerJoin(player);
    }
}
