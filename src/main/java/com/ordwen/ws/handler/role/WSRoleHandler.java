package com.ordwen.ws.handler.role;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

public interface WSRoleHandler {
    String getType();
    JsonObject buildRequest(Player player, String[] args);
    void handleResponse(JsonObject response);
    void handleError(Throwable ex);
}
