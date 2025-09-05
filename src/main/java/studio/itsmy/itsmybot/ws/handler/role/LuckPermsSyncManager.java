package studio.itsmy.itsmybot.ws.handler.role;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Coordinates LuckPerms mutations with the WebSocket role sync workflow.
 * <p>
 * When roles are applied programmatically (following a bot response), LuckPerms will emit
 * {@link NodeMutateEvent}s. This manager tracks "expected" mutations (ADD/REMOVE on groups)
 * so that only <em>untracked</em> changes trigger a {@link RoleSyncUtil#sendRoleSyncUpdate}.
 *
 * <h2>How it works</h2>
 * <ol>
 *   <li>Before applying groups via Vault/LuckPerms, call {@link #registerExpectedMutation(UUID, String, RoleChangeEvent.Action)}
 *       for each group change that the plugin is about to make.</li>
 *   <li>{@link #init(LuckPerms)} subscribes to {@link NodeMutateEvent} and compares
 *       the actual node delta (added/removed groups) to the expected map.</li>
 *   <li>If a group change is not in the expected map, a role sync update is sent to the bot
 *       to keep external systems in sync.</li>
 * </ol>
 */
public class LuckPermsSyncManager {

    private final ItsMyBotPlugin plugin;

    /**
     * For each player UUID, tracks expected mutations keyed by lower-cased group name,
     * mapped to the action (ADD/REMOVE) we anticipate.
     */
    private final Map<UUID, Map<String, RoleChangeEvent.Action>> expectedMutations = new ConcurrentHashMap<>();

    /**
     * Creates a new sync manager.
     *
     * @param plugin main plugin instance
     */
    public LuckPermsSyncManager(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Subscribes to LuckPerms {@link NodeMutateEvent}s.
     *
     * @param luckPerms LuckPerms API entry point
     */
    public void init(LuckPerms luckPerms) {
        luckPerms.getEventBus().subscribe(plugin, NodeMutateEvent.class, this::onNodeMutate);
    }

    /**
     * Handles node mutations and detects untracked role changes.
     * <p>
     * Computes the delta of inheritance nodes (groups) between before/after snapshots,
     * consumes matching {@link #expectedMutations} entries, and if any remaining change
     * is untracked, triggers a {@link RoleSyncUtil#sendRoleSyncUpdate}.
     *
     * @param event the node mutate event
     */
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

        // delta = (after - before) / (before - after)
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

    /**
     * Extracts group names from a collection of nodes (only {@link InheritanceNode}s).
     *
     * @param nodes LuckPerms nodes snapshot
     * @return set of group names
     */
    private Set<String> getGroupNames(Collection<? extends Node> nodes) {
        return nodes.stream()
                .filter(InheritanceNode.class::isInstance)
                .map(node -> ((InheritanceNode) node).getGroupName())
                .collect(Collectors.toSet());
    }

    /**
     * Registers an expected group mutation for a player.
     * <p>
     * Must be called <strong>before</strong> applying the change via Vault/LuckPerms
     * (e.g., {@code playerAddGroup}/{@code playerRemoveGroup}).
     *
     * @param uuid   player UUID
     * @param group  group name
     * @param action expected action (ADD/REMOVE)
     */
    public void registerExpectedMutation(UUID uuid, String group, RoleChangeEvent.Action action) {
        expectedMutations
                .computeIfAbsent(uuid, u -> new ConcurrentHashMap<>())
                .put(group.toLowerCase(), action);
    }

    /**
     * Consumes a matching expected mutation if present.
     *
     * @return {@code true} if the mutation was expected and removed from the map
     */
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