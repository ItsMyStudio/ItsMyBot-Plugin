package com.ordwen.ws.handlers;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

public interface WSCommandHandler {
    String getType();
    JsonObject buildRequest(Player player, String[] args);
    void handleResponse(Player player, JsonObject response);
    void handleError(Player player, Throwable ex);
}
