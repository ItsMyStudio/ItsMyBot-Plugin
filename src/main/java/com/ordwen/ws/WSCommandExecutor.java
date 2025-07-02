package com.ordwen.ws;

import com.google.gson.JsonObject;
import com.ordwen.ItsMyBotPlugin;
import com.ordwen.ws.handlers.WSCommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WSCommandExecutor {

    private final ItsMyBotPlugin plugin;

    public WSCommandExecutor(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(Player player, String[] args, WSCommandHandler handler) {
        final WSClient client = plugin.getWSClient();
        final JsonObject message = handler.buildRequest(player, args);

        if (!message.has("id")) {
            message.addProperty("id", UUID.randomUUID().toString());
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                client.sendRequest(message, message.get("id").getAsString())
                        .thenAccept(response -> Bukkit.getScheduler().runTask(plugin, () ->
                                handler.handleResponse(player, response)
                        ))
                        .exceptionally(ex -> {
                            Bukkit.getScheduler().runTask(plugin, () ->
                                    handler.handleError(player, ex)
                            );
                            return null;
                        })
        );
    }
}
