package studio.itsmy.itsmybot.service;

import com.google.gson.JsonObject;
import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.enumeration.LogType;
import studio.itsmy.itsmybot.ws.handler.LogWSHandler;
import org.bukkit.entity.Player;

/**
 * Service responsible for logging significant server and player events.
 * <p>
 * This class centralizes the creation of log payloads and delegates
 * their delivery to {@link LogWSHandler}, which sends them to the WebSocket server.
 *
 * <p>Logged events include:
 * <ul>
 *     <li>Server start and stop</li>
 *     <li>Player join and leave</li>
 *     <li>Player command executions</li>
 * </ul>
 */
public class LogService {

    private final ItsMyBotPlugin plugin;
    private final LogWSHandler logHandler = new LogWSHandler();

    /**
     * Creates a new {@code LogService}.
     *
     * @param plugin the main plugin instance
     */
    public LogService(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Logs a server start event.
     * <p>
     * Sends a {@link LogType#SERVER_START} log with a message.
     */
    public void logServerStart() {
        final JsonObject details = new JsonObject();
        details.addProperty("message", "Server has started.");
        logHandler.sendLog(plugin, LogType.SERVER_START, null, null, details);
    }

    /**
     * Logs a server stop event.
     * <p>
     * Sends a {@link LogType#SERVER_STOP} log with a message.
     */
    public void logServerStop() {
        final JsonObject details = new JsonObject();
        details.addProperty("message", "Server is stopping.");
        logHandler.sendLog(plugin, LogType.SERVER_STOP, null, null, details);
    }

    /**
     * Logs a player join event.
     * <p>
     * Includes the player's IP address, UUID, and name.
     *
     * @param player the player who joined
     */
    public void logPlayerJoin(Player player) {
        final JsonObject details = new JsonObject();
        details.addProperty("ip", player.getAddress().getAddress().getHostAddress());
        logHandler.sendLog(plugin, LogType.PLAYER_JOIN, player.getUniqueId(), player.getName(), details);
    }

    /**
     * Logs a player leave event.
     * <p>
     * Includes the player's UUID and name.
     *
     * @param player the player who left
     */
    public void logPlayerLeave(Player player) {
        final JsonObject details = new JsonObject();
        logHandler.sendLog(plugin, LogType.PLAYER_LEAVE, player.getUniqueId(), player.getName(), details);
    }

    /**
     * Logs a player command execution.
     * <p>
     * Includes the executed command string, along with the player's UUID and name.
     *
     * @param player  the player who executed the command
     * @param command the command string (without leading slash)
     */
    public void logPlayerCommand(Player player, String command) {
        final JsonObject details = new JsonObject();
        details.addProperty("command", command);
        logHandler.sendLog(plugin, LogType.PLAYER_COMMAND, player.getUniqueId(), player.getName(), details);
    }
}