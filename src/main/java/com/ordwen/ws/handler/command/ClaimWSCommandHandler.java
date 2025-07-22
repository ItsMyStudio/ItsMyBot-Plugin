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
            player.sendMessage(Messages.CLAIM_SUCCESS.toString());

        } else if ("CLAIM_FAIL".equals(type)) {
            final String reason = response.has("reason") ? response.get("reason").getAsString() : "UNKNOWN";

            switch (reason) {
                case "NO_REWARD":
                    player.sendMessage(Messages.CLAIM_NO_REWARD.toString());
                    break;
                case "NOT_LINKED":
                    player.sendMessage(Messages.NOT_LINKED.toString());
                    break;
                default:
                    PluginLogger.error("Unknown claim failure reason: " + reason);
                    player.sendMessage(Messages.ERROR_OCCURRED.toString());
            }

        } else {
            PluginLogger.error("Unexpected response type: " + type);
            player.sendMessage(Messages.ERROR_OCCURRED.toString());
        }
    }

    @Override
    public void handleError(Player player, Throwable ex) {
        PluginLogger.error("WebSocket CLAIM error: " + ex.getMessage());
        player.sendMessage(Messages.ERROR_OCCURRED.toString());
    }

    private void handleRewards(Player player, JsonArray rewardsJson) {
        for (JsonElement element : rewardsJson) {
            final JsonObject rewardObj = element.getAsJsonObject();

            if (rewardObj.has("message")) {
                player.sendMessage(TextFormatter.format(rewardObj.get("message").getAsString()));
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
                        player.sendMessage(TextFormatter.format(content));
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
