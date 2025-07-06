package com.ordwen.listeners;

import com.ordwen.ItsMyBotPlugin;
import net.milkbowl.vault.permission.Permission;
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
        final Permission permission = plugin.getPermission();

        String[] groups = permission.getPlayerGroups(event.getPlayer());
        System.out.println("Player " + event.getPlayer().getName() + " has groups: " + String.join(", ", groups));

        permission.playerAddGroup(event.getPlayer(), "vip");
        groups = permission.getPlayerGroups(event.getPlayer());
        System.out.println("After adding 'vip', player " + event.getPlayer().getName() + " has groups: " + String.join(", ", groups));

        permission.playerRemoveGroup(event.getPlayer(), "vip");
        groups = permission.getPlayerGroups(event.getPlayer());
        System.out.println("After removing 'vip', player " + event.getPlayer().getName() + " has groups: " + String.join(", ", groups));
    }
}
