package com.ordwen.ws.handler;

import com.google.gson.JsonObject;
import com.ordwen.ItsMyBotPlugin;
import com.ordwen.enumeration.LogType;
import com.ordwen.util.PluginLogger;
import com.ordwen.ws.handler.command.WSCommandHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public class LogWSHandler implements WSCommandHandler {

    @Override
    public String getType() {
        return "LOG";
    }

    @Override
    public JsonObject buildRequest(Player player, String[] args) {
        throw new UnsupportedOperationException("LogWSHandler does not support buildRequest via player commands. Use sendLog method instead.");
    }

    @Override
    public void handleResponse(Player player, JsonObject response) {
        throw new UnsupportedOperationException("LOG does not support handling responses. It is a request-only handler.");
    }

    @Override
    public void handleError(Player player, Throwable ex) {
        PluginLogger.error("Failed to send log to bot: " + ex.getMessage());
    }

    /**
     * Send a log event to the bot via WSClient
     */
    public void sendLog(ItsMyBotPlugin plugin, LogType logType, @Nullable UUID playerUuid, @Nullable String playerName, JsonObject details) {
        final JsonObject message = new JsonObject();
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
