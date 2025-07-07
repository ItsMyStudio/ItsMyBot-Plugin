package com.ordwen.service;

import com.google.gson.JsonObject;
import com.ordwen.ItsMyBotPlugin;
import com.ordwen.enumeration.LogType;
import com.ordwen.ws.handler.LogWSHandler;
import org.bukkit.entity.Player;

public class LogService {

    private final ItsMyBotPlugin plugin;
    private final LogWSHandler logHandler = new LogWSHandler();

    public LogService(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    public void logServerStart() {
        final JsonObject details = new JsonObject();
        details.addProperty("message", "Server has started.");
        logHandler.sendLog(plugin, LogType.SERVER_START, null, null, details);
    }

    public void logServerStop() {
        final JsonObject details = new JsonObject();
        details.addProperty("message", "Server is stopping.");
        logHandler.sendLog(plugin, LogType.SERVER_STOP, null, null, details);
    }

    public void logPlayerJoin(Player player) {
        final JsonObject details = new JsonObject();
        details.addProperty("ip", player.getAddress().getAddress().getHostAddress());
        logHandler.sendLog(plugin, LogType.PLAYER_JOIN, player.getUniqueId(), player.getName(), details);
    }

    public void logPlayerLeave(Player player) {
        final JsonObject details = new JsonObject();
        logHandler.sendLog(plugin, LogType.PLAYER_LEAVE, player.getUniqueId(), player.getName(), details);
    }

    public void logPlayerCommand(Player player, String command) {
        final JsonObject details = new JsonObject();
        details.addProperty("command", command);
        logHandler.sendLog(plugin, LogType.PLAYER_COMMAND, player.getUniqueId(), player.getName(), details);
    }
}