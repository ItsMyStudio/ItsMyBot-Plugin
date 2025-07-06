package com.ordwen.ws.handler.role;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ordwen.ItsMyBotPlugin;
import com.ordwen.enumeration.Messages;
import com.ordwen.util.PluginLogger;
import org.bukkit.entity.Player;

public class SyncErrorWSCommandHandler implements WSRoleHandler {

    private final ItsMyBotPlugin plugin;

    public SyncErrorWSCommandHandler(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getType() {
        return "SYNC_ERROR";
    }

    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        throw new UnsupportedOperationException("SYNC_ERROR does not support building requests. It is a response-only handler.");
    }

    @Override
    public void handleResponse(JsonObject response) {
        final Player player = plugin.getServer().getPlayer(response.get("uuid").getAsString());
        if (player == null) {
            PluginLogger.warn("It looks like the player is offline or does not exist. Cannot handle SYNC_ERROR response.");
            return;
        }

        final String errorCode = response.has("error_code") ? response.get("error_code").getAsString() : "UNKNOWN_ERROR";
        final JsonObject details = response.has("details") ? response.getAsJsonObject("details") : null;

        PluginLogger.error("Received SYNC_ERROR for player " + player.getName() + ": " + errorCode);

        if ("UNKNOWN_ROLE".equals(errorCode) && details != null && details.has("unknown_roles")) {
            final JsonArray unknownRoles = details.getAsJsonArray("unknown_roles");
            final StringBuilder builder = new StringBuilder();
            for (JsonElement roleElem : unknownRoles) {
                final String role = roleElem.getAsString();
                builder.append(role).append(", ");
            }
            player.sendMessage(Messages.SYNC_ERROR_UNKNOWN_ROLE.toString().replace("%roles%", builder.toString()));
        } else {
            player.sendMessage(Messages.ERROR_OCCURRED.toString());
        }
    }

    @Override
    public void handleError(Throwable ex) {
        PluginLogger.error("WebSocket SYNC_ERROR error: " + ex.getMessage());
    }
}