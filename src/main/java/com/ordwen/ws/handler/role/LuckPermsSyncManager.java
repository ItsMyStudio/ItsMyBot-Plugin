package com.ordwen.ws.handler.role;

import com.google.gson.JsonObject;
import com.ordwen.ItsMyBotPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.node.NodeMutateEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LuckPermsSyncManager {

    private final ItsMyBotPlugin plugin;
    private final Set<UUID> suppressedSyncs = ConcurrentHashMap.newKeySet();

    public LuckPermsSyncManager(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    public void init(LuckPerms luckPerms) {
        luckPerms.getEventBus().subscribe(plugin, NodeMutateEvent.class, this::onNodeMutate);
    }

    private void onNodeMutate(NodeMutateEvent event) {
        final String name = event.getTarget().getFriendlyName();
        if (name == null || name.isEmpty()) return;

        final Player targetPlayer = plugin.getServer().getPlayer(name);
        if (targetPlayer == null || !targetPlayer.isOnline()) return;

        final UUID uuid = targetPlayer.getUniqueId();
        if (suppressedSyncs.contains(uuid)) return;

        final Player player = plugin.getServer().getPlayer(event.getTarget().getFriendlyName());
        if (player != null && player.isOnline()) {
            RoleSyncUtil.sendRoleSyncUpdate(plugin, player);
        }
    }

    public void suppress(UUID uuid) {
        suppressedSyncs.add(uuid);
    }

    public void unsuppress(UUID uuid) {
        suppressedSyncs.remove(uuid);
    }

    public boolean isSuppressed(UUID uuid) {
        return suppressedSyncs.contains(uuid);
    }
}