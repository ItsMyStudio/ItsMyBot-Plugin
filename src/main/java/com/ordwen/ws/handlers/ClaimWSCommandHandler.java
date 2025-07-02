package com.ordwen.ws.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ordwen.configuration.essentials.WSConfig;
import com.ordwen.enums.Messages;
import com.ordwen.utils.PluginLogger;
import com.ordwen.utils.TextFormatter;
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
        request.addProperty("server_id", WSConfig.getServerId());
        return request;
    }

    @Override
    public void handleResponse(Player player, JsonObject response) {
        final String type = response.get("type").getAsString();
        if ("CLAIM_SUCCESS".equals(type)) {
            if (response.has("rewards")) {
                final JsonArray rewardsJson = response.getAsJsonArray("rewards");
                handleRewards(player, rewardsJson);
            } else {
                PluginLogger.warn("CLAIM_SUCCESS response missing 'rewards' array.");
            }

            player.sendMessage(Messages.CLAIM_SUCCESS.toString());
        } else if ("CLAIM_NO_REWARD".equals(type)) {
            player.sendMessage(Messages.CLAIM_NO_REWARD.toString());
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
            final String type = rewardObj.get("type").getAsString();

            switch (type.toUpperCase()) {
                case "MESSAGE":
                    final String msg = rewardObj.get("message").getAsString();
                    player.sendMessage(TextFormatter.format(msg));
                    break;
                case "COMMAND":
                    final JsonArray commandArray = rewardObj.getAsJsonArray("commands");
                    for (JsonElement cmd : commandArray) {
                        final String command = cmd.getAsString().replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                    break;
                default:
                    PluginLogger.warn("Unknown reward type: " + type);
            }
        }
    }
}
