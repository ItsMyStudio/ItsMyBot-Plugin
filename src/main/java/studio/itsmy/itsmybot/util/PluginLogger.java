package studio.itsmy.itsmybot.util;

import java.util.logging.Logger;

/**
 * Utility class for standardized plugin logging.
 * <p>
 * Provides static methods to log messages at different levels
 * (info, warning, error) under the {@code ItsMyBot-Plugin} logger name.
 */
public class PluginLogger {

    /** Internal logger instance. */
    private static final Logger logger = Logger.getLogger("ItsMyBot-Plugin");

    /** Private constructor to prevent instantiation. */
    private PluginLogger() {}

    /**
     * Logs an informational message to the console.
     *
     * @param msg the message to log
     */
    public static void info(String msg) {
        logger.info(msg);
    }

    /**
     * Logs a warning message to the console.
     *
     * @param msg the message to log
     */
    public static void warn(String msg) {
        logger.warning(msg);
    }

    /**
     * Logs an error message to the console.
     *
     * @param msg the message to log
     */
    public static void error(String msg) {
        logger.severe(msg);
    }
}