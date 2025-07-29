package com.ordwen.ws.handler.command;

import com.google.gson.JsonObject;
import com.ordwen.enumeration.Messages;
import com.ordwen.util.PluginLogger;
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
        request.addProperty("code", args[0]);
        return request;
    }

    @Override
    public void handleResponse(Player player, JsonObject response) {
        final String type = response.get("type").getAsString();
        if ("LINK_SUCCESS".equals(type)) {
           Messages.LINK_SUCCESS.send(player);
        } else if ("LINK_FAIL".equals(type)) {
            final String reason = response.get("reason").getAsString();
            switch (reason) {
                case "INVALID_CODE":
                   Messages.INVALID_CODE.send(player);
                    break;
                case "ALREADY_LINKED":
                   Messages.ALREADY_LINKED.send(player);
                    break;
                default:
                    PluginLogger.error("Unknown failure reason: " + reason);
                   Messages.ERROR_OCCURRED.send(player);
            }
        } else {
            PluginLogger.error("Unexpected response type: " + type);
           Messages.ERROR_OCCURRED.send(player);
        }
    }

    @Override
    public void handleError(Player player, Throwable ex) {
        PluginLogger.error("WebSocket LINK error: " + ex.getMessage());
       Messages.ERROR_OCCURRED.send(player);
    }
}