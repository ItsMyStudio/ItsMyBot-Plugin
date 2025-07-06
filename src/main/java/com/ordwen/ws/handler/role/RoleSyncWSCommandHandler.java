package com.ordwen.ws.handler.role;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ordwen.ItsMyBotPlugin;
import com.ordwen.util.PluginLogger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

public class RoleSyncWSCommandHandler implements WSRoleHandler {

    private final ItsMyBotPlugin plugin;

    public RoleSyncWSCommandHandler(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getType() {
        return "ROLES_SYNC";
    }

    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        throw new UnsupportedOperationException("ROLES_SYNC does not support building requests. It is a response-only handler.");
    }

    @Override
    public void handleResponse(JsonObject response) {
        final Permission perm = plugin.getPermission();
        if (perm == null) {
            return;
        }

        final Player player = plugin.getServer().getPlayer(response.get("uuid").getAsString());
        if (player == null) {
            PluginLogger.warn("It looks like the player is offline or does not exist. Cannot handle ROLES_SYNC response.");
            return;
        }

        final String direction = response.has("direction") ? response.get("direction").getAsString() : "";
        if (!"DISCORD_TO_MC".equals(direction)) {
            PluginLogger.warn("Ignored ROLES_SYNC message with wrong direction: " + direction);
            return;
        }

        if (!response.has("uuid") || !response.get("uuid").getAsString().equals(player.getUniqueId().toString())) {
            PluginLogger.warn("ROLES_SYNC UUID mismatch or missing");
            return;
        }

        if (response.has("add")) {
            final JsonArray addArray = response.getAsJsonArray("add");
            for (JsonElement e : addArray) {
                final String role = e.getAsString();
                perm.playerAddGroup(player, role);
            }
        }

        if (response.has("remove")) {
            final JsonArray removeArray = response.getAsJsonArray("remove");
            for (JsonElement e : removeArray) {
                final String role = e.getAsString();
                perm.playerRemoveGroup(player, role);
            }
        }
    }

    @Override
    public void handleError(Throwable ex) {
        PluginLogger.error("WebSocket ROLES_SYNC error: " + ex.getMessage());
    }
}
