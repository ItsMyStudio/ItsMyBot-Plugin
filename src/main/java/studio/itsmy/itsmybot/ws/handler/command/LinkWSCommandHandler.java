package studio.itsmy.itsmybot.ws.handler.command;

import com.google.gson.JsonObject;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.util.PluginLogger;
import org.bukkit.entity.Player;

/**
 * WebSocket handler for linking a player's Discord account using a code.
 * <p>
 * <strong>Precondition:</strong> {@code args[0]} must contain the link code.
 *
 * <p>Request schema:
 * <pre>{@code
 * {
 *   "type": "LINK",
 *   "player_uuid": "<uuid>",
 *   "player_name": "<name>",
 *   "code": "<link_code>"
 * }
 * }</pre>
 *
 * <p>Expected responses:
 * <ul>
 *   <li>{@code LINK_SUCCESS} → {@link Messages#LINK_SUCCESS}</li>
 *   <li>{@code LINK_FAIL} with {@code reason}:
 *       <ul>
 *         <li>{@code INVALID_CODE} → {@link Messages#INVALID_CODE}</li>
 *         <li>{@code ALREADY_LINKED} → {@link Messages#ALREADY_LINKED}</li>
 *         <li>other → logs error + {@link Messages#ERROR_OCCURRED}</li>
 *       </ul>
 *   </li>
 * </ul>
 */
public class LinkWSCommandHandler implements WSCommandHandler {

    @Override
    public String getType() {
        return "LINK";
    }

    /**
     * Builds a LINK request with UUID, name and the provided link code.
     */
    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        final JsonObject request = new JsonObject();
        request.addProperty("type", getType());
        request.addProperty("player_uuid", player.getUniqueId().toString());
        request.addProperty("player_name", player.getName());
        request.addProperty("code", args[0]);
        return request;
    }

    /**
     * Interprets {@code LINK_SUCCESS} or {@code LINK_FAIL} with a reason.
     */
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

    /**
     * Logs the error and notifies the player with a generic message.
     */
    @Override
    public void handleError(Player player, Throwable ex) {
        PluginLogger.error("WebSocket LINK error: " + ex.getMessage());
       Messages.ERROR_OCCURRED.send(player);
    }
}