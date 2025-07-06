package com.ordwen.listener;

import com.google.gson.JsonObject;
import com.ordwen.ItsMyBotPlugin;
import com.ordwen.ws.handler.role.FullRoleSyncWSCommandHandler;
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

        final FullRoleSyncWSCommandHandler fullSyncHandler = new FullRoleSyncWSCommandHandler(plugin);
        final JsonObject request = fullSyncHandler.buildRequest(player, new String[0]);
        if (request == null) {
            return;
        }

        plugin.getWSClient().sendMessage(request.toString());
    }
}
