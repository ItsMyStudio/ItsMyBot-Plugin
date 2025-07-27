package com.ordwen.ws.handler.role;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ordwen.ItsMyBotPlugin;
import com.ordwen.configuration.essential.WSConfig;
import com.ordwen.util.PluginLogger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RoleSyncUtil {

    private static final String TYPE_FULL_SYNC = "FULL_ROLE_SYNC";
    private static final String TYPE_UPDATE = "ROLE_SYNC_UPDATE";
    private static final String TYPE_SUCCESS = "ROLE_SYNC_SUCCESS";
    private static final String TYPE_FAIL = "ROLE_SYNC_FAIL";

    private RoleSyncUtil() {}

    public static void sendFullRoleSync(ItsMyBotPlugin plugin, OfflinePlayer player) {
        sendRoleSync(plugin, player, TYPE_FULL_SYNC);
    }

    public static void sendRoleSyncUpdate(ItsMyBotPlugin plugin, OfflinePlayer player) {
        sendRoleSync(plugin, player, TYPE_UPDATE);
    }

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

    private static void handleRoleSyncResponse(ItsMyBotPlugin plugin, OfflinePlayer player, JsonObject response) {
        final Permission perm = plugin.getPermission();
        if (perm == null) return;

        System.out.println("Handling role sync response: " + response);

        final String responseType = response.get("type").getAsString();
        switch (responseType) {
            case TYPE_SUCCESS:
                applyRoleChanges(perm, player, response);
                break;
            case TYPE_FAIL:
                logRoleSyncFailure(player, response);
                break;
            default:
                PluginLogger.warn("Unhandled role sync response: " + response);
        }
    }

    private static void applyRoleChanges(Permission perm, OfflinePlayer player, JsonObject response) {
        applyRoleDelta(perm, player, response, "add", true);
        applyRoleDelta(perm, player, response, "remove", false);
    }

    private static void applyRoleDelta(Permission perm, OfflinePlayer player, JsonObject json, String key, boolean add) {
        System.out.println("Applying role delta: " + key + " for player: " + player.getName());
        if (!json.has(key)) {
            System.out.println("No roles to " + (add ? "add" : "remove") + " for player: " + player.getName());
            return;
        }
        for (JsonElement e : json.getAsJsonArray(key)) {
            final String role = e.getAsString();
            if (add) {
                System.out.println("Adding role: " + role + " to player: " + player.getName());
                perm.playerAddGroup("global", player, role);
            } else {
                System.out.println("Removing role: " + role + " from player: " + player.getName());
                perm.playerRemoveGroup("global", player, role);
            }
        }
    }

    private static void logRoleSyncFailure(OfflinePlayer player, JsonObject response) {
        final String reason = response.has("reason") ? response.get("reason").getAsString() : "UNKNOWN";
        PluginLogger.warn("Role sync failed for " + player.getName() + ": " + reason);
    }

    public static void handleSyncRole(ItsMyBotPlugin plugin, JsonObject message) {
        final Permission perm = plugin.getPermission();
        if (perm == null) return;

        System.out.println("Handling SYNC_ROLE message: " + message);
        final String uuidStr = message.get("player_uuid").getAsString();
        final OfflinePlayer player = plugin.getServer().getOfflinePlayer(UUID.fromString(uuidStr));
        if (player == null || !player.hasPlayedBefore()) {
            PluginLogger.warn("Player not found for SYNC_ROLE: " + uuidStr);
            return;
        }

        final LuckPermsSyncManager syncManager = plugin.getLpSyncManager();
        syncManager.suppress(uuid);
        try {
            applyRoleDelta(perm, player, message, "add", true);
            applyRoleDelta(perm, player, message, "remove", false);
        } finally {
            syncManager.unsuppress(uuid);
        }
    }
}
