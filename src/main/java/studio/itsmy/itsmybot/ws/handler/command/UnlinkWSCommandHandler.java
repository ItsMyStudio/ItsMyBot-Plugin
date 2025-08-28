package studio.itsmy.itsmybot.ws.handler.command;

import com.google.gson.JsonObject;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.util.PluginLogger;
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
        return request;
    }

    @Override
    public void handleResponse(Player player, JsonObject response) {
        final String type = response.get("type").getAsString();
        if ("UNLINK_SUCCESS".equals(type)) {
            Messages.UNLINK_SUCCESS.send(player);
        } else if ("UNLINK_FAIL".equals(type)) {
            final String reason = response.get("reason").getAsString();
            if ("NOT_LINKED".equals(reason)) {
                Messages.NOT_LINKED.send(player);
            } else {
                Messages.ERROR_OCCURRED.send(player);
                PluginLogger.error("Unlink failed: " + reason + " for player " + player.getName());
            }
        } else {
            player.sendMessage("Unexpected response type: " + type);
            Messages.ERROR_OCCURRED.send(player);
        }
    }

    @Override
    public void handleError(Player player, Throwable ex) {
        PluginLogger.error("WebSocket UNLINK error: " + ex.getMessage());
        Messages.ERROR_OCCURRED.send(player);
    }
}