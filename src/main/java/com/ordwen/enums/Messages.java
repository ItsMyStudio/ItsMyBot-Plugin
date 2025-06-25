package com.ordwen.enums;

import com.ordwen.configuration.essentials.Prefix;
import com.ordwen.files.implementations.MessagesFile;
import com.ordwen.utils.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public enum Messages {

    PLAYER_ONLY("player_only", "This command can only be executed by a player!"),
    NO_PERMISSION("no_permission", "You do not have permission to execute this command!"),
    PLAYER_HELP("player_help", " . . . ")
    ;

    private final String path;
    private final String defaultMessage;

    Messages(String path, String message) {
        this.path = path;
        this.defaultMessage = message;
    }

    @Override
    public String toString() {
        String msg = MessagesFile.getInstance().get(this.path, defaultMessage);

        if (msg.trim().isEmpty()) return "";
        else return TextFormatter.format(null, Prefix.getPrefix() + msg);
    }

    public String getMessage(Player player) {
        String msg = MessagesFile.getInstance().get(this.path, defaultMessage);

        if (msg.trim().isEmpty()) return null;
        else return TextFormatter.format(player, Prefix.getPrefix() + msg);
    }

    public String getMessage(String playerName) {
        final Player player = Bukkit.getPlayer(playerName);
        if (player == null) return null;

        String msg = MessagesFile.getInstance().get(this.path, defaultMessage);
        if (msg.trim().isEmpty()) return null;

        else return TextFormatter.format(player, Prefix.getPrefix() + msg);
    }

    public String getMessage(Player player, Map<String, String> placeholders) {
        String msg = MessagesFile.getInstance().get(this.path, defaultMessage);
        if (msg.trim().isEmpty()) return null;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace(entry.getKey(), entry.getValue());
        }

        return TextFormatter.format(player, Prefix.getPrefix() + msg);
    }

    /**
     * Get the default value of the path.
     *
     * @return the default value of the path.
     */
    public String getDefault() {
        return this.defaultMessage;
    }

    /**
     * Get the path to the string.
     *
     * @return the path to the string.
     */
    public String getPath() {
        return this.path;
    }
}
