package com.ordwen.itsmybot.ws.handler.role;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ordwen.itsmybot.ItsMyBotPlugin;
import com.ordwen.itsmybot.configuration.essential.WSConfig;
import com.ordwen.itsmybot.util.PluginLogger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

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

    private static void applyRoleChanges(ItsMyBotPlugin plugin, OfflinePlayer player, JsonObject response) {
        final Permission perm = plugin.getPermission();
        if (perm == null) return;

        final UUID uuid = player.getUniqueId();
        final List<String> toAdd = extractStringList(response, "add");
        final List<String> toRemove = extractStringList(response, "remove");

        final LuckPermsSyncManager syncManager = plugin.getLpSyncManager();
        registerExpectedMutations(player, perm, uuid, toAdd, toRemove, syncManager);
    }

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

    private static void applyRoleDelta(Permission perm, OfflinePlayer player, List<String> roles, boolean add) {
        for (String role : roles) {
            if (add) {
                perm.playerAddGroup("global", player, role);
            } else {
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