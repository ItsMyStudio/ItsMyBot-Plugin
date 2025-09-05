package studio.itsmy.itsmybot.ws.handler.command;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

/**
 * Contract for WebSocket command handlers.
 * <p>
 * A handler is responsible for:
 * <ul>
 *   <li>Declaring its message {@link #getType() type} (e.g. {@code "LINK"}).</li>
 *   <li>Building the outbound JSON request from a player context and arguments.</li>
 *   <li>Handling the backend JSON response on success.</li>
 *   <li>Handling transport/protocol errors (timeouts, failures).</li>
 * </ul>
 *
 * <p>Handlers are typically used by {@code WSCommandExecutor}.
 */
public interface WSCommandHandler {

    /**
     * Returns the WebSocket message type handled by this implementation.
     *
     * @return a non-null type string (e.g. {@code "LINK"})
     */
    String getType();

    /**
     * Builds an outbound WebSocket request for this handler.
     *
     * @param player the invoking player (never null)
     * @param args   command arguments (may be empty)
     * @return a JSON request with at least the {@code type} field set
     */
    JsonObject buildRequest(Player player, String[] args);

    /**
     * Handles a successful response from the backend.
     *
     * @param player   the invoking player
     * @param response the backend JSON response
     */
    void handleResponse(Player player, JsonObject response);

    /**
     * Handles an error that occurred while sending or awaiting the response.
     *
     * @param player the invoking player
     * @param ex     the thrown error (timeout, I/O, etc.)
     */
    void handleError(Player player, Throwable ex);
}
