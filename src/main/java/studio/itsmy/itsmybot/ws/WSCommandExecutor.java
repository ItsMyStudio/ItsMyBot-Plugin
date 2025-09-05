package studio.itsmy.itsmybot.ws;

import com.google.gson.JsonObject;
import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.configuration.essential.WSConfig;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.ws.handler.command.WSCommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Executor responsible for sending WebSocket requests on behalf of players and handling responses.
 * <p>
 * This class bridges the gap between command logic and the {@link WSClient},
 * ensuring that requests are:
 * <ul>
 *   <li>Properly built and enriched with required fields ({@code id}, {@code server_id})</li>
 *   <li>Sent asynchronously to avoid blocking the main server thread</li>
 *   <li>Handled gracefully on success or failure (back on the main thread)</li>
 * </ul>
 *
 * <p>The {@link WSCommandHandler} implementation is responsible for:
 * <ul>
 *   <li>Building the outgoing JSON request in {@link WSCommandHandler#buildRequest(Player, String[])}</li>
 *   <li>Handling the successful response in {@link WSCommandHandler#handleResponse(Player, JsonObject)}</li>
 *   <li>Handling errors or timeouts in {@link WSCommandHandler#handleError(Player, Throwable)}</li>
 * </ul>
 */
public class WSCommandExecutor {

    private final ItsMyBotPlugin plugin;

    /**
     * Creates a new {@code WSCommandExecutor}.
     *
     * @param plugin the main plugin instance
     */
    public WSCommandExecutor(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes a WebSocket command on behalf of a player.
     * <p>
     * Steps performed:
     * <ol>
     *   <li>Checks that the {@link WSClient} exists and is {@link WSClient#isReady() ready}.</li>
     *   <li>Builds a JSON message using the provided {@link WSCommandHandler}.</li>
     *   <li>Ensures that the message contains an {@code id} (generating a random UUID if missing).</li>
     *   <li>Ensures that the message contains the {@code server_id} field.</li>
     *   <li>Sends the request asynchronously and handles the result:</li>
     *   <ul>
     *     <li>On success: invokes {@link WSCommandHandler#handleResponse(Player, JsonObject)} on the main thread.</li>
     *     <li>On failure or timeout: invokes {@link WSCommandHandler#handleError(Player, Throwable)} on the main thread.</li>
     *   </ul>
     * </ol>
     *
     * @param player  the player initiating the command
     * @param args    command arguments passed by the player
     * @param handler the handler responsible for building the request and processing the response
     */
    public void execute(Player player, String[] args, WSCommandHandler handler) {
        final WSClient client = plugin.getWSClient();

        // If WebSocket is not connected or authenticated, notify player on main thread
        if (client == null || !client.isReady()) {
            Bukkit.getScheduler().runTask(plugin, () -> Messages.BOT_NOT_CONNECTED.send(player));
            return;
        }

        final JsonObject message = handler.buildRequest(player, args);

        // Ensure message has an ID for request-response correlation
        if (!message.has("id")) {
            message.addProperty("id", UUID.randomUUID().toString());
        }

        // Ensure message has a server identifier
        if (!message.has("server_id")) {
            message.addProperty("server_id", WSConfig.getServerId());
        }

        // Send the request asynchronously to avoid blocking the main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                client.sendRequest(message, message.get("id").getAsString())
                        .thenAccept(response -> Bukkit.getScheduler().runTask(plugin, () ->
                                handler.handleResponse(player, response)
                        ))
                        .exceptionally(ex -> {
                            Bukkit.getScheduler().runTask(plugin, () ->
                                    handler.handleError(player, ex)
                            );
                            return null;
                        })
        );
    }
}
