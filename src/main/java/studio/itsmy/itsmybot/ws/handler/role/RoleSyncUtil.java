package studio.itsmy.itsmybot.ws.handler.role;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.luckperms.api.event.node.NodeMutateEvent;
import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.configuration.essential.WSConfig;
import studio.itsmy.itsmybot.util.PluginLogger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * Utility methods for initiating and applying role synchronizations with the WebSocket backend.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Build and send role sync requests (full or update) with current player groups.</li>
 *   <li>Handle responses:
 *       <ul>
 *         <li>{@code ROLE_SYNC_SUCCESS}: apply add/remove deltas via Vault</li>
 *         <li>{@code ROLE_SYNC_FAIL}: log reason</li>
 *       </ul>
 *   </li>
 *   <li>Handle incoming {@code SYNC_ROLE} push messages to apply server-side changes.</li>
 * </ul>
 *
 * <h2>JSON schema (request → response)</h2>
 * <pre>{@code
 * // Request (FULL_ROLE_SYNC or ROLE_SYNC_UPDATE)
 * {
 *   "type": "FULL_ROLE_SYNC" | "ROLE_SYNC_UPDATE",
 *   "server_id": "...",
 *   "player_uuid": "...",
 *   "roles": ["group1", "group2", ...]
 * }
 *
 * // Success response
 * {
 *   "type": "ROLE_SYNC_SUCCESS",
 *   "add":    ["groupX", ...],
 *   "remove": ["groupY", ...]
 * }
 *
 * // Failure response
 * { "type": "ROLE_SYNC_FAIL", "reason": "..." }
 * }</pre>
 */
public final class RoleSyncUtil {

    private static final String TYPE_FULL_SYNC = "FULL_ROLE_SYNC";
    private static final String TYPE_UPDATE = "ROLE_SYNC_UPDATE";
    private static final String TYPE_SUCCESS = "ROLE_SYNC_SUCCESS";
    private static final String TYPE_FAIL = "ROLE_SYNC_FAIL";

    /** Private constructor to prevent instantiation. */
    private RoleSyncUtil() {
    }

    /**
     * Sends a full role sync request for the given player (on join, for example).
     *
     * @param plugin plugin instance
     * @param player target player (online or offline)
     */
    public static void sendFullRoleSync(ItsMyBotPlugin plugin, OfflinePlayer player) {
        sendRoleSync(plugin, player, TYPE_FULL_SYNC);
    }

    /**
     * Sends a differential update request (triggered on untracked local changes).
     *
     * @param plugin plugin instance
     * @param player target player
     */
    public static void sendRoleSyncUpdate(ItsMyBotPlugin plugin, OfflinePlayer player) {
        sendRoleSync(plugin, player, TYPE_UPDATE);
    }

    /**
     * Builds and sends a role sync request, then processes the async response.
     *
     * @param plugin plugin instance
     * @param player player
     * @param type   {@code FULL_ROLE_SYNC} or {@code ROLE_SYNC_UPDATE}
     */
    private static void sendRoleSync(ItsMyBotPlugin plugin, OfflinePlayer player, String type) {
        final Permission permission = plugin.getPermission();
        if (permission == null) return;

        final JsonObject request = new JsonObject();
        request.addProperty("type", type);
        request.addProperty("server_id", WSConfig.getServerId());
        request.addProperty("player_uuid", player.getUniqueId().toString());

        final JsonArray roles = Arrays.stream(permission.getPlayerGroups("global", player))
                .map(JsonPrimitive::new)
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
        request.add("roles", roles);

        final String id = UUID.randomUUID().toString();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                plugin.getWSClient().sendRequest(request, id)
                        .thenAccept(response -> Bukkit.getScheduler().runTask(plugin, () ->
                                handleRoleSyncResponse(plugin, player, response)))
                        .exceptionally(ex -> {
                            Bukkit.getScheduler().runTask(plugin, () ->
                                    PluginLogger.error("WebSocket role sync error: " + ex.getMessage()));
                            return null;
                        })
        );
    }

    /**
     * Handles a role sync response from the backend.
     * <p>
     * On success, extracts add/remove lists and applies them via Vault, after registering
     * expected mutations in {@link LuckPermsSyncManager} to avoid feedback loops.
     */
    private static void handleRoleSyncResponse(ItsMyBotPlugin plugin, OfflinePlayer player, JsonObject response) {
        final Permission perm = plugin.getPermission();
        if (perm == null) return;

        final String responseType = response.get("type").getAsString();
        switch (responseType) {
            case TYPE_SUCCESS:
                applyRoleChanges(plugin, player, response);
                break;
            case TYPE_FAIL:
                logRoleSyncFailure(player, response);
                break;
            default:
                PluginLogger.warn("Unhandled role sync response: " + response);
        }
    }

    /**
     * Registers expected mutations then applies group deltas via Vault.
     * <p>
     * This ordering is <strong>critical</strong>: mutations must be registered before calling
     * {@code playerAddGroup}/{@code playerRemoveGroup} to ensure {@link NodeMutateEvent}
     * recognizes them as expected.
     */
    private static void applyRoleChanges(ItsMyBotPlugin plugin, OfflinePlayer player, JsonObject response) {
        final Permission perm = plugin.getPermission();
        if (perm == null) return;

        final UUID uuid = player.getUniqueId();
        final List<String> toAdd = extractStringList(response, "add");
        final List<String> toRemove = extractStringList(response, "remove");

        final LuckPermsSyncManager syncManager = plugin.getLpSyncManager();
        registerExpectedMutations(player, perm, uuid, toAdd, toRemove, syncManager);
    }

    /**
     * Registers expected mutations and applies them via Vault.
     *
     * @param player      target player
     * @param perm        Vault Permission service
     * @param uuid        player uuid
     * @param toAdd       groups to add
     * @param toRemove    groups to remove
     * @param syncManager sync manager to mark expected mutations
     */
    private static void registerExpectedMutations(OfflinePlayer player, Permission perm, UUID uuid, List<String> toAdd, List<String> toRemove, LuckPermsSyncManager syncManager) {
        for (String role : toAdd) {
            syncManager.registerExpectedMutation(uuid, role, RoleChangeEvent.Action.ADD);
        }
        for (String role : toRemove) {
            syncManager.registerExpectedMutation(uuid, role, RoleChangeEvent.Action.REMOVE);
        }

        applyRoleDelta(perm, player, toAdd, true);
        applyRoleDelta(perm, player, toRemove, false);
    }

    /**
     * Applies group mutations through Vault.
     *
     * @param add if {@code true}, add groups; otherwise remove groups
     */
    private static void applyRoleDelta(Permission perm, OfflinePlayer player, List<String> roles, boolean add) {
        for (String role : roles) {
            if (add) {
                perm.playerAddGroup("global", player, role);
            } else {
                perm.playerRemoveGroup("global", player, role);
            }
        }
    }

    /**
     * Logs a sync failure with a reason if provided.
     */
    private static void logRoleSyncFailure(OfflinePlayer player, JsonObject response) {
        final String reason = response.has("reason") ? response.get("reason").getAsString() : "UNKNOWN";
        PluginLogger.warn("Role sync failed for " + player.getName() + ": " + reason);
    }

    /**
     * Handles an incoming push message {@code SYNC_ROLE} from the backend.
     * <p>
     * Extracts add/remove lists, registers them as expected, then applies them via Vault.
     *
     * @param plugin  plugin instance
     * @param message JSON message containing {@code player_uuid}, {@code add}, {@code remove}
     */
    public static void handleSyncRole(ItsMyBotPlugin plugin, JsonObject message) {
        final Permission perm = plugin.getPermission();
        if (perm == null) return;

        final UUID uuid = UUID.fromString(message.get("player_uuid").getAsString());
        final OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);
        if (player == null || !player.hasPlayedBefore()) {
            return;
        }

        final LuckPermsSyncManager syncManager = plugin.getLpSyncManager();

        final List<String> toAdd = extractStringList(message, "add");
        final List<String> toRemove = extractStringList(message, "remove");

        registerExpectedMutations(player, perm, uuid, toAdd, toRemove, syncManager);
    }

    /**
     * Utility to extract a string list from a JSON array field.
     */
    private static List<String> extractStringList(JsonObject json, String key) {
        if (!json.has(key)) return Collections.emptyList();
        final JsonArray array = json.getAsJsonArray(key);
        final List<String> result = new ArrayList<>(array.size());
        for (JsonElement e : array) {
            result.add(e.getAsString());
        }
        return result;
    }
}