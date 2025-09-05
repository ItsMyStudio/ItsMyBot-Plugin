package studio.itsmy.itsmybot.enumeration;

/**
 * Represents the different types of events that can be logged by the plugin.
 * <p>
 * This enum is primarily used by the logging system to categorize log entries
 * (e.g., server lifecycle events, player connections, command executions).
 */
public enum LogType {
    SERVER_START,
    SERVER_STOP,
    PLAYER_JOIN,
    PLAYER_LEAVE,
    PLAYER_COMMAND
}
