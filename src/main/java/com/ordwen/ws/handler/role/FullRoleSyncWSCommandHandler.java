package com.ordwen.ws.handler.role;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ordwen.ItsMyBotPlugin;
import com.ordwen.configuration.essential.WSConfig;
import com.ordwen.enumeration.Messages;
import com.ordwen.util.PluginLogger;
import com.ordwen.ws.handler.command.WSCommandHandler;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

public class FullRoleSyncWSCommandHandler implements WSRoleHandler {

    private final ItsMyBotPlugin plugin;

    public FullRoleSyncWSCommandHandler(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getType() {
        return "FULL_ROLE_SYNC";
    }

    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        final Permission perm = plugin.getPermission();
        if (perm == null) {
            return null;
        }

        final JsonObject request = new JsonObject();
        request.addProperty("type", getType());
        request.addProperty("direction", "MC_TO_DISCORD");
        request.addProperty("uuid", player.getUniqueId().toString());

        String[] currentGroups = perm.getPlayerGroups(player);

        final JsonArray rolesArray = new JsonArray();
        for (String group : currentGroups) {
            rolesArray.add(new JsonPrimitive(group));
        }

        request.add("roles", rolesArray);
        request.addProperty("server_id", WSConfig.getServerId());

        return request;
    }

    @Override
    public void handleResponse(JsonObject response) {
        throw new UnsupportedOperationException("FULL_ROLE_SYNC does not support handling responses. It is a request-only handler.");
    }

    @Override
    public void handleError(Throwable ex) {
        PluginLogger.error("WebSocket FULL_ROLE_SYNC error: " + ex.getMessage());
    }
}