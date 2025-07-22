package com.ordwen.ws.handler.command;

import com.google.gson.JsonObject;
import com.ordwen.configuration.essential.WSConfig;
import com.ordwen.enumeration.Messages;
import com.ordwen.util.PluginLogger;
import org.bukkit.entity.Player;

public class UnlinkWSCommandHandler implements WSCommandHandler {

    @Override
    public String getType() {
        return "UNLINK";
    }

    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        final JsonObject request = new JsonObject();
        request.addProperty("type", getType());
        request.addProperty("player_uuid", player.getUniqueId().toString());
        request.addProperty("server_id", WSConfig.getServerId());
        return request;
    }

    @Override
    public void handleResponse(Player player, JsonObject response) {
        final String type = response.get("type").getAsString();
        if ("UNLINK_SUCCESS".equals(type)) {
            player.sendMessage(Messages.UNLINK_SUCCESS.toString());
        } else if ("UNLINK_FAIL".equals(type)) {
            final String reason = response.get("reason").getAsString();
            if ("NOT_LINKED".equals(reason)) {
                player.sendMessage(Messages.NOT_LINKED.toString());
            } else {
                player.sendMessage(Messages.ERROR_OCCURRED.toString());
                PluginLogger.error("Unlink failed: " + reason + " for player " + player.getName());
            }
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