package studio.itsmy.itsmybot.ws;

import com.google.gson.JsonObject;
import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.configuration.essential.WSConfig;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.ws.handler.command.WSCommandHandler;
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

        if (client == null || !client.isReady()) {
            Bukkit.getScheduler().runTask(plugin, () -> Messages.BOT_NOT_CONNECTED.send(player));
            return;
        }

        final JsonObject message = handler.buildRequest(player, args);

        if (!message.has("id")) {
            message.addProperty("id", UUID.randomUUID().toString());
        }

        if (!message.has("server_id")) {
            message.addProperty("server_id", WSConfig.getServerId());
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
