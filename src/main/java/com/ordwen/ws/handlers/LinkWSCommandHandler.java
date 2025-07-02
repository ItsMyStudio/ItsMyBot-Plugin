package com.ordwen.ws.handlers;

import com.google.gson.JsonObject;
import com.ordwen.configuration.essentials.WSConfig;
import com.ordwen.enums.Messages;
import com.ordwen.utils.PluginLogger;
import org.bukkit.entity.Player;

public class LinkWSCommandHandler implements WSCommandHandler {

    @Override
    public String getType() {
        return "LINK";
    }

    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        final JsonObject request = new JsonObject();
        request.addProperty("type", getType());
        request.addProperty("player_uuid", player.getUniqueId().toString());
        request.addProperty("player_name", player.getName());
        request.addProperty("code", args[1]);
        request.addProperty("server_id", WSConfig.getServerId());
        return request;
    }

    @Override
    public void handleResponse(Player player, JsonObject response) {
        final String type = response.get("type").getAsString();
        if ("LINK_SUCCESS".equals(type)) {
            player.sendMessage(Messages.LINK_SUCCESS.toString());
        } else if ("LINK_FAIL".equals(type)) {
            final String reason = response.get("reason").getAsString();
            switch (reason) {
                case "INVALID_CODE":
                    player.sendMessage(Messages.INVALID_CODE.toString());
                    break;
                case "ALREADY_LINKED":
                    player.sendMessage(Messages.ALREADY_LINKED.toString());
                    break;
                default:
                    PluginLogger.error("Unknown failure reason: " + reason);
                    player.sendMessage(Messages.ERROR_OCCURRED.toString());
            }
        } else {
            PluginLogger.error("Unexpected response type: " + type);
            player.sendMessage(Messages.ERROR_OCCURRED.toString());
        }
    }

    @Override
    public void handleError(Player player, Throwable ex) {
        PluginLogger.error("WebSocket LINK error: " + ex.getMessage());
        player.sendMessage(Messages.ERROR_OCCURRED.toString());
    }
}