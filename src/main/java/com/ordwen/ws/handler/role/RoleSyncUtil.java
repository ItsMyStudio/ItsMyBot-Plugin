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
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public final class RoleSyncUtil {

    private static final String TYPE_FULL_SYNC = "FULL_ROLE_SYNC";
    private static final String TYPE_UPDATE = "ROLE_SYNC_UPDATE";
    private static final String TYPE_SUCCESS = "ROLE_SYNC_SUCCESS";
    private static final String TYPE_FAIL = "ROLE_SYNC_FAIL";


    private RoleSyncUtil() {}

    public static void sendFullRoleSync(ItsMyBotPlugin plugin, Player player) {
        sendRoleSync(plugin, player, TYPE_FULL_SYNC);
    }

    public static void sendRoleSyncUpdate(ItsMyBotPlugin plugin, Player player) {
        sendRoleSync(plugin, player, TYPE_UPDATE);
    }

    private static void sendRoleSync(ItsMyBotPlugin plugin, Player player, String type) {
        final Permission permission = plugin.getPermission();
        if (permission == null) return;

        final JsonObject request = new JsonObject();
        request.addProperty("type", type);
        request.addProperty("server_id", WSConfig.getServerId());
        request.addProperty("player_uuid", player.getUniqueId().toString());

        final JsonArray roles = Arrays.stream(permission.getPlayerGroups(player))
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

    private static void handleRoleSyncResponse(ItsMyBotPlugin plugin, Player player, JsonObject response) {
        final Permission perm = plugin.getPermission();
        if (perm == null) return;

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

    private static void applyRoleChanges(Permission perm, Player player, JsonObject response) {
        applyRoleDelta(perm, player, response, "add", true);
        applyRoleDelta(perm, player, response, "remove", false);
    }

    private static void applyRoleDelta(Permission perm, Player player, JsonObject json, String key, boolean add) {
        if (!json.has(key)) return;
        for (JsonElement e : json.getAsJsonArray(key)) {
            final String role = e.getAsString();
            if (add) {
                perm.playerAddGroup(player, role);
            } else {
                perm.playerRemoveGroup(player, role);
            }
        }
    }

    private static void logRoleSyncFailure(Player player, JsonObject response) {
        final String reason = response.has("reason") ? response.get("reason").getAsString() : "UNKNOWN";
        PluginLogger.warn("Role sync failed for " + player.getName() + ": " + reason);
    }

    public static void handleSyncRole(ItsMyBotPlugin plugin, JsonObject message) {
        final Permission perm = plugin.getPermission();
        if (perm == null) return;

        final String uuidStr = message.get("player_uuid").getAsString();
        final Player player = plugin.getServer().getPlayer(UUID.fromString(uuidStr));
        if (player == null) {
            PluginLogger.warn("Player not found for SYNC_ROLE: " + uuidStr);
            return;
        }

        applyRoleDelta(perm, player, message, "add", true);
        applyRoleDelta(perm, player, message, "remove", false);
    }
}
