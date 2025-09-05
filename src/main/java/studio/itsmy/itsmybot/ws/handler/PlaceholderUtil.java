package studio.itsmy.itsmybot.ws.handler;

import com.google.gson.JsonObject;
import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.configuration.essential.WSConfig;
import studio.itsmy.itsmybot.util.PluginUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import studio.itsmy.itsmybot.ws.WSClient;

/**
 * Utility class for processing placeholder resolution requests received via WebSocket.
 * <p>
 * This class is used by {@link WSClient} when receiving a {@code PLACEHOLDER} message.
 * It extracts the requested placeholder and player UUID (if present),
 * resolves it using PlaceholderAPI (or returns the raw value if PAPI is unavailable),
 * and sends a JSON response back to the WebSocket server.
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Supports resolving placeholders globally or for a specific player.</li>
 *   <li>Performs resolution on the Bukkit main thread for thread-safety.</li>
 *   <li>Handles player-not-found and internal errors gracefully by returning error JSON.</li>
 * </ul>
 *
 * <h2>Example message flow</h2>
 * <pre>{@code
 * // Incoming message from WS server:
 * {
 *   "type": "PLACEHOLDER",
 *   "id": "123e4567-e89b-12d3-a456-426614174000",
 *   "placeholder": "%player_name%",
 *   "player_uuid": "550e8400-e29b-41d4-a716-446655440000"
 * }
 *
 * // Outgoing result:
 * {
 *   "type": "PLACEHOLDER_RESULT",
 *   "id": "...",
 *   "server_id": "...",
 *   "placeholder": "%player_name%",
 *   "player_uuid": "...",
 *   "value": "Ordwen"
 * }
 * }</pre>
 */
public final class PlaceholderUtil {

    /** Private constructor to prevent instantiation. */
    private PlaceholderUtil() {
    }

    /**
     * Handles an incoming placeholder request from the WebSocket server.
     * <p>
     * Steps performed:
     * <ol>
     *   <li>Extracts the {@code id}, {@code placeholder}, and {@code player_uuid} (if present).</li>
     *   <li>Schedules resolution on the main server thread.</li>
     *   <li>Attempts to resolve the placeholder using {@link #resolvePlaceholder(ItsMyBotPlugin, String, String)}.</li>
     *   <li>On success: sends a {@code PLACEHOLDER_RESULT} message via {@link WSClient#sendResponse(JsonObject, String)}.</li>
     *   <li>On player not found: tries a server-wide resolution as fallback, or sends a {@code PLAYER_NOT_FOUND} error.</li>
     *   <li>On any other exception: sends an {@code INTERNAL_ERROR} message with the exception message.</li>
     * </ol>
     *
     * @param plugin  main plugin instance
     * @param message JSON object containing the placeholder request
     */
    public static void handlePlaceholderRequest(ItsMyBotPlugin plugin, JsonObject message) {
        final String id = message.get("id").getAsString();
        final String rawPlaceholder = message.get("placeholder").getAsString();
        final String uuidStr = extractUuid(message);

        Bukkit.getScheduler().runTask(plugin, () -> {
            final JsonObject base = baseOut(id, rawPlaceholder, uuidStr);

            try {
                final String resolved = resolvePlaceholder(plugin, uuidStr, rawPlaceholder);
                sendResult(plugin, base, resolved);
            } catch (PlayerNotFound e) {
                try {
                    final String resolved = PlaceholderAPI.setPlaceholders(null, rawPlaceholder);
                    sendResult(plugin, base, resolved);
                } catch (Exception ex) {
                    sendError(plugin, base, "PLAYER_NOT_FOUND", null);
                }
            } catch (Exception e) {
                sendError(plugin, base, "INTERNAL_ERROR", e.getMessage());
            }
        });
    }

    /**
     * Extracts the optional {@code player_uuid} from the incoming message.
     *
     * @param message JSON message
     * @return UUID string, or {@code null} if absent
     */
    private static String extractUuid(JsonObject message) {
        return (message.has("player_uuid") && !message.get("player_uuid").isJsonNull())
                ? message.get("player_uuid").getAsString()
                : null;
    }

    /**
     * Builds the base JSON object for the outgoing response (result or error).
     *
     * @param id          request id
     * @param placeholder original placeholder string
     * @param uuidStr     player UUID string (nullable)
     * @return JSON object with base fields populated
     */
    private static JsonObject baseOut(String id, String placeholder, String uuidStr) {
        final JsonObject out = new JsonObject();
        out.addProperty("id", id);
        out.addProperty("server_id", WSConfig.getServerId());
        out.addProperty("placeholder", placeholder);
        if (uuidStr != null) out.addProperty("player_uuid", uuidStr);
        return out;
    }

    /**
     * Resolves a placeholder for a given player UUID (or globally if UUID is null).
     *
     * @param plugin         main plugin instance
     * @param uuidStr        player UUID string (nullable)
     * @param rawPlaceholder raw placeholder string
     * @return resolved placeholder value
     * @throws PlayerNotFound if a UUID is provided but the player cannot be found (never played and offline)
     */
    private static String resolvePlaceholder(ItsMyBotPlugin plugin, String uuidStr, String rawPlaceholder) {
        if (!PluginUtils.isPluginEnabled("PlaceholderAPI")) {
            return rawPlaceholder;
        }

        if (uuidStr == null) {
            return PlaceholderAPI.setPlaceholders(null, rawPlaceholder); // server-wide
        }

        final OfflinePlayer offline = plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(uuidStr));
        if (offline == null || (!offline.hasPlayedBefore() && !offline.isOnline())) {
            throw new PlayerNotFound();
        }

        final Player online = offline.getPlayer();
        return (online != null)
                ? PlaceholderAPI.setPlaceholders(online, rawPlaceholder)
                : PlaceholderAPI.setPlaceholders(offline, rawPlaceholder);
    }

    /**
     * Sends a successful placeholder resolution result back to the WebSocket server.
     *
     * @param plugin   main plugin instance
     * @param base     base JSON object containing id, server_id, placeholder, player_uuid
     * @param resolved resolved value (may be {@code null}, will be converted to empty string)
     */
    private static void sendResult(ItsMyBotPlugin plugin, JsonObject base, String resolved) {
        base.addProperty("type", "PLACEHOLDER_RESULT");
        base.addProperty("value", resolved == null ? "" : resolved);
        plugin.getWSClient().sendResponse(base, base.get("id").getAsString());
    }

    /**
     * Sends an error message back to the WebSocket server.
     *
     * @param plugin  main plugin instance
     * @param base    base JSON object containing id, server_id, placeholder, player_uuid
     * @param reason  error reason (e.g., {@code PLAYER_NOT_FOUND}, {@code INTERNAL_ERROR})
     * @param message optional human-readable error message (nullable)
     */
    private static void sendError(ItsMyBotPlugin plugin, JsonObject base, String reason, String message) {
        base.addProperty("type", "PLACEHOLDER_ERROR");
        base.addProperty("reason", reason);
        if (message != null) base.addProperty("message", message);
        plugin.getWSClient().sendResponse(base, base.get("id").getAsString());
    }

    /**
     * Internal runtime exception used to signal that a player could not be resolved.
     */
    private static final class PlayerNotFound extends RuntimeException {
    }
}