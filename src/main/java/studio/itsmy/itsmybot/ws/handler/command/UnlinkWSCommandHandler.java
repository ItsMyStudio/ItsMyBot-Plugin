package studio.itsmy.itsmybot.ws.handler.command;

import com.google.gson.JsonObject;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.util.PluginLogger;
import org.bukkit.entity.Player;

/**
 * WebSocket handler for unlinking a player's Discord account.
 * <p>
 * Request schema:
 * <pre>{@code
 * { "type": "UNLINK", "player_uuid": "<uuid>" }
 * }</pre>
 *
 * <p>Expected responses:
 * <ul>
 *   <li>{@code UNLINK_SUCCESS} → sends {@link Messages#UNLINK_SUCCESS}</li>
 *   <li>{@code UNLINK_FAIL} with {@code reason}:
 *       <ul>
 *         <li>{@code NOT_LINKED} → {@link Messages#NOT_LINKED}</li>
 *         <li>other → {@link Messages#ERROR_OCCURRED} and logs details</li>
 *       </ul>
 *   </li>
 * </ul>
 */
public class UnlinkWSCommandHandler implements WSCommandHandler {

    @Override
    public String getType() {
        return "UNLINK";
    }

    /**
     * Builds the UNLINK request JSON object with player UUID.
     */
    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        final JsonObject request = new JsonObject();
        request.addProperty("type", getType());
        request.addProperty("player_uuid", player.getUniqueId().toString());
        return request;
    }

    /**
     * Handles the UNLINK response, sending appropriate messages to the player
     * based on success or failure reasons.
     */
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

    /**
     * Logs the error and notifies the player with a generic message.
     */
    @Override
    public void handleError(Player player, Throwable ex) {
        PluginLogger.error("WebSocket UNLINK error: " + ex.getMessage());
        Messages.ERROR_OCCURRED.send(player);
    }
}