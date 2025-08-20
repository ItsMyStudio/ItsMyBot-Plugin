package com.ordwen.itsmybot.ws.handler;

import com.google.gson.JsonObject;
import com.ordwen.itsmybot.ItsMyBotPlugin;
import com.ordwen.itsmybot.configuration.essential.WSConfig;
import com.ordwen.itsmybot.util.PluginUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class PlaceholderUtil {

    private PlaceholderUtil() {
    }

    public static void handlePlaceholderRequest(ItsMyBotPlugin plugin, JsonObject message) {
        final String id = message.get("id").getAsString();
        final String rawPlaceholder = message.get("placeholder").getAsString();
        final String uuidStr = extractUuid(message);

        Bukkit.getScheduler().runTask(plugin, () -> {
            final JsonObject base = baseOut(id, rawPlaceholder, uuidStr);

            try {
                final String resolved = resolvePlaceholder(plugin, uuidStr, rawPlaceholder);
                sendResult(plugin, base, resolved);
            } catch (PlayerNotFound e) {
                try {
                    final String resolved = PlaceholderAPI.setPlaceholders(null, rawPlaceholder);
                    sendResult(plugin, base, resolved);
                } catch (Exception ex) {
                    sendError(plugin, base, "PLAYER_NOT_FOUND", null);
                }
            } catch (Exception e) {
                sendError(plugin, base, "INTERNAL_ERROR", e.getMessage());
            }
        });
    }

    private static String extractUuid(JsonObject message) {
        return (message.has("player_uuid") && !message.get("player_uuid").isJsonNull())
                ? message.get("player_uuid").getAsString()
                : null;
    }

    private static JsonObject baseOut(String id, String placeholder, String uuidStr) {
        final JsonObject out = new JsonObject();
        out.addProperty("id", id);
        out.addProperty("server_id", WSConfig.getServerId());
        out.addProperty("placeholder", placeholder);
        if (uuidStr != null) out.addProperty("player_uuid", uuidStr);
        return out;
    }

    private static String resolvePlaceholder(ItsMyBotPlugin plugin, String uuidStr, String rawPlaceholder) {
        if (!PluginUtils.isPluginEnabled("PlaceholderAPI")) {
            return rawPlaceholder;
        }

        if (uuidStr == null) {
            return PlaceholderAPI.setPlaceholders(null, rawPlaceholder); // server-wide
        }

        final OfflinePlayer offline = plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(uuidStr));
        if (offline == null || (!offline.hasPlayedBefore() && !offline.isOnline())) {
            throw new PlayerNotFound();
        }

        final Player online = offline.getPlayer();
        return (online != null)
                ? PlaceholderAPI.setPlaceholders(online, rawPlaceholder)
                : PlaceholderAPI.setPlaceholders(offline, rawPlaceholder);
    }

    private static void sendResult(ItsMyBotPlugin plugin, JsonObject base, String resolved) {
        base.addProperty("type", "PLACEHOLDER_RESULT");
        base.addProperty("value", resolved == null ? "" : resolved);
        plugin.getWSClient().sendResponse(base, base.get("id").getAsString());
    }

    private static void sendError(ItsMyBotPlugin plugin, JsonObject base, String reason, String message) {
        base.addProperty("type", "PLACEHOLDER_ERROR");
        base.addProperty("reason", reason);
        if (message != null) base.addProperty("message", message);
        plugin.getWSClient().sendResponse(base, base.get("id").getAsString());
    }

    private static final class PlayerNotFound extends RuntimeException {
    }
}