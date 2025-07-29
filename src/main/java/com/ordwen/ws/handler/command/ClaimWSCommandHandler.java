package com.ordwen.ws.handler.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ordwen.enumeration.Messages;
import com.ordwen.util.PluginLogger;
import com.ordwen.util.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ClaimWSCommandHandler implements WSCommandHandler {

    @Override
    public String getType() {
        return "CLAIM";
    }

    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        final JsonObject request = new JsonObject();
        request.addProperty("type", getType());
        request.addProperty("player_uuid", player.getUniqueId().toString());
        return request;
    }

    @Override
    public void handleResponse(Player player, JsonObject response) {
        final String type = response.get("type").getAsString();

        if ("CLAIM_SUCCESS".equals(type)) {
            if (response.has("rewards")) {
                final JsonArray rewards = response.getAsJsonArray("rewards");
                handleRewards(player, rewards);
            } else {
                PluginLogger.warn("CLAIM_SUCCESS response missing 'rewards' array.");
            }
            Messages.CLAIM_SUCCESS.send(player);

        } else if ("CLAIM_FAIL".equals(type)) {
            final String reason = response.has("reason") ? response.get("reason").getAsString() : "UNKNOWN";

            switch (reason) {
                case "NO_REWARD":
                    Messages.CLAIM_NO_REWARD.send(player);
                    break;
                case "NOT_LINKED":
                    Messages.NOT_LINKED.send(player);
                    break;
                default:
                    PluginLogger.error("Unknown claim failure reason: " + reason);
                    Messages.ERROR_OCCURRED.send(player);
            }

        } else {
            PluginLogger.error("Unexpected response type: " + type);
            Messages.ERROR_OCCURRED.send(player);
        }
    }

    @Override
    public void handleError(Player player, Throwable ex) {
        PluginLogger.error("WebSocket CLAIM error: " + ex.getMessage());
        Messages.ERROR_OCCURRED.send(player);
    }

    private void handleRewards(Player player, JsonArray rewardsJson) {
        for (JsonElement element : rewardsJson) {
            final JsonObject rewardObj = element.getAsJsonObject();

            if (rewardObj.has("message")) {
                TextFormatter.send(player, rewardObj.get("message").getAsString());
            }

            if (!rewardObj.has("actions")) {
                continue;
            }

            final JsonArray actions = rewardObj.getAsJsonArray("actions");

            for (JsonElement actionElement : actions) {
                final String rawAction = actionElement.getAsString().trim();
                final int firstSpace = rawAction.indexOf(']');
                if (!rawAction.startsWith("[") || firstSpace == -1) {
                    PluginLogger.warn("Malformed reward action: " + rawAction);
                    continue;
                }

                final String prefix = rawAction.substring(1, firstSpace).trim().toLowerCase(); // message, console, player
                final String content = rawAction.substring(firstSpace + 1).trim().replace("%player%", player.getName());

                switch (prefix) {
                    case "message":
                        TextFormatter.send(player, content);
                        break;
                    case "console":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), content);
                        break;
                    case "player":
                        player.performCommand(content);
                        break;
                    default:
                        PluginLogger.warn("Unknown reward action type: " + prefix);
                }
            }
        }
    }
}
