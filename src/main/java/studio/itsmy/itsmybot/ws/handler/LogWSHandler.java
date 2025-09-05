package studio.itsmy.itsmybot.ws.handler;

import com.google.gson.JsonObject;
import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.configuration.essential.WSConfig;
import studio.itsmy.itsmybot.enumeration.LogType;
import studio.itsmy.itsmybot.util.PluginLogger;
import studio.itsmy.itsmybot.ws.WSClient;
import studio.itsmy.itsmybot.ws.handler.command.WSCommandHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket command handler for sending log events to the bot.
 * <p>
 * This implementation of {@link WSCommandHandler} is <strong>write-only</strong>:
 * it does not support building requests from player commands or handling responses.
 * Instead, it provides a dedicated {@link #sendLog(ItsMyBotPlugin, LogType, UUID, String, JsonObject)}
 * method to push structured log events (server start/stop, player actions) to the backend.
 *
 * <h2>Key characteristics</h2>
 * <ul>
 *   <li>Type: {@code "LOG"}</li>
 *   <li>No request building via player input (throws {@link UnsupportedOperationException})</li>
 *   <li>No response handling (logs are fire-and-forget)</li>
 *   <li>Error handling simply logs a message to the console</li>
 * </ul>
 */
public class LogWSHandler implements WSCommandHandler {

    /**
     * {@inheritDoc}
     * <p>
     * Always returns {@code "LOG"} to indicate this handler's message type.
     */
    @Override
    public String getType() {
        return "LOG";
    }

    /**
     * This handler does not support building requests from player commands.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        throw new UnsupportedOperationException("LogWSHandler does not support buildRequest via player commands. Use sendLog method instead.");
    }

    /**
     * This handler does not expect or process any responses.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void handleResponse(Player player, JsonObject response) {
        throw new UnsupportedOperationException("LOG does not support handling responses. It is a request-only handler.");
    }

    /**
     * Handles errors that occur while attempting to send a log.
     * <p>
     * Logs the error message to the console.
     *
     * @param player the player associated with the failed request (may be {@code null})
     * @param ex     the exception thrown
     */
    @Override
    public void handleError(Player player, Throwable ex) {
        PluginLogger.error("Failed to send log to bot: " + ex.getMessage());
    }

    /**
     * Sends a structured log event to the WebSocket server.
     * <p>
     * The JSON message includes:
     * <ul>
     *   <li>{@code server_id}: server identifier from {@link WSConfig}</li>
     *   <li>{@code type}: always {@code "LOG"}</li>
     *   <li>{@code log_type}: enum name from {@link LogType}</li>
     *   <li>{@code timestamp}: ISO-8601 timestamp of event emission</li>
     *   <li>{@code uuid} (optional): player's UUID</li>
     *   <li>{@code player_name} (optional): player's name</li>
     *   <li>{@code details}: extra event-specific data</li>
     * </ul>
     *
     * @param plugin     the plugin instance used to get the active {@link WSClient}
     * @param logType    type of log event (e.g., {@link LogType#PLAYER_JOIN})
     * @param playerUuid optional player UUID associated with the event
     * @param playerName optional player name associated with the event
     * @param details    additional data describing the event
     */
    public void sendLog(ItsMyBotPlugin plugin, LogType logType, @Nullable UUID playerUuid, @Nullable String playerName, JsonObject details) {
        final JsonObject message = new JsonObject();
        message.addProperty("server_id", WSConfig.getServerId());
        message.addProperty("type", getType());
        message.addProperty("log_type", logType.name());
        message.addProperty("timestamp", Instant.now().toString());

        if (playerUuid != null) {
            message.addProperty("uuid", playerUuid.toString());
        }
        if (playerName != null) {
            message.addProperty("player_name", playerName);
        }

        message.add("details", details);

        plugin.getWSClient().sendMessage(message.toString());
    }
}
