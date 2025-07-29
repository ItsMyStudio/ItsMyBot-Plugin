package com.ordwen.itsmybot.util;

import java.util.logging.Logger;

public class PluginLogger {

    private PluginLogger() {}

    private static final Logger logger = Logger.getLogger("ItsMyBot-Plugin");

    /**
     * Logs an informational message to the console.
     *
     * @param msg the message to log.
     */
    public static void info(String msg) {
        logger.info(msg);
    }

    /**
     * Logs a warning message to the console.
     *
     * @param msg the message to log.
     */
    public static void warn(String msg) {
        logger.warning(msg);
    }

    /**
     * Logs an error message to the console.
     *
     * @param msg the message to log.
     */
    public static void error(String msg) {
        logger.severe(msg);
    }
}