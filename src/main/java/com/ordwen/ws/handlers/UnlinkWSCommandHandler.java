package com.ordwen.ws.handlers;

import com.google.gson.JsonObject;
import com.ordwen.configuration.essentials.WSConfig;
import com.ordwen.enums.Messages;
import com.ordwen.utils.PluginLogger;
import org.bukkit.entity.Player;

public class UnlinkWSCommandHandler implements WSCommandHandler{

    @Override
    public String getType() {
        return "UNLINK";
    }

    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        final JsonObject request = new JsonObject();
        request.addProperty("type", getType());
        request.addProperty("player_uuid", player.getName());
        request.addProperty("server_id", WSConfig.getServerId());
        return request;
    }

    @Override
    public void handleResponse(Player player, JsonObject response) {
        final String type = response.get("type").getAsString();
        if ("UNLINK_SUCCESS".equals(type)) {
            player.sendMessage("You have been successfully unlinked.");
        } else if ("UNLINK_FAIL".equals(type)) {
            final String reason = response.get("reason").getAsString();
            player.sendMessage("Unlink failed: " + reason);
        } else {
            player.sendMessage("Unexpected response type: " + type);
            player.sendMessage(Messages.ERROR_OCCURRED.toString());
        }
    }

    @Override
    public void handleError(Player player, Throwable ex) {
        PluginLogger.error("WebSocket UNLINK error: " + ex.getMessage());
        player.sendMessage(Messages.ERROR_OCCURRED.toString());
    }
}
