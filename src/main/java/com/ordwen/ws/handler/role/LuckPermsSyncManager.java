package com.ordwen.ws.handler.role;

import com.ordwen.ItsMyBotPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LuckPermsSyncManager {

    private final ItsMyBotPlugin plugin;

    private final Map<UUID, Map<String, RoleChangeEvent.Action>> expectedMutations = new ConcurrentHashMap<>();

    public LuckPermsSyncManager(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    public void init(LuckPerms luckPerms) {
        luckPerms.getEventBus().subscribe(plugin, NodeMutateEvent.class, this::onNodeMutate);
    }

    private void onNodeMutate(NodeMutateEvent event) {
        final String name = event.getTarget().getFriendlyName();
        if (name.isEmpty()) return;

        final Player player = plugin.getServer().getPlayer(name);
        if (player == null || !player.isOnline()) return;

        final UUID uuid = player.getUniqueId();

        final Set<String> added = getGroupNames(event.getDataAfter());
        final Set<String> removed = getGroupNames(event.getDataBefore());
        final Set<String> actuallyAdded = new HashSet<>(added);
        final Set<String> actuallyRemoved = new HashSet<>(removed);

        actuallyAdded.removeAll(removed);
        actuallyRemoved.removeAll(added);

        boolean hasUntrackedMutation = false;

        for (String group : actuallyAdded) {
            if (!consumeExpected(uuid, group, RoleChangeEvent.Action.ADD)) {
                hasUntrackedMutation = true;
            }
        }

        for (String group : actuallyRemoved) {
            if (!consumeExpected(uuid, group, RoleChangeEvent.Action.REMOVE)) {
                hasUntrackedMutation = true;
            }
        }

        if (hasUntrackedMutation) {
            RoleSyncUtil.sendRoleSyncUpdate(plugin, player);
        }
    }

    private Set<String> getGroupNames(Collection<? extends Node> nodes) {
        return nodes.stream()
                .filter(InheritanceNode.class::isInstance)
                .map(node -> ((InheritanceNode) node).getGroupName())
                .collect(Collectors.toSet());
    }

    public void registerExpectedMutation(UUID uuid, String group, RoleChangeEvent.Action action) {
        expectedMutations
                .computeIfAbsent(uuid, u -> new ConcurrentHashMap<>())
                .put(group.toLowerCase(), action);
    }

    private boolean consumeExpected(UUID uuid, String group, RoleChangeEvent.Action action) {
        final Map<String, RoleChangeEvent.Action> map = expectedMutations.get(uuid);
        if (map == null) return false;

        final RoleChangeEvent.Action expected = map.get(group.toLowerCase());
        if (expected == action) {
            map.remove(group.toLowerCase());
            if (map.isEmpty()) {
                expectedMutations.remove(uuid);
            }
            return true;
        }
        return false;
    }
}