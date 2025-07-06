package com.ordwen.enumeration;

import com.ordwen.configuration.essential.Prefix;
import com.ordwen.file.implementation.MessagesFile;
import com.ordwen.util.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public enum Messages {

    PLAYER_ONLY("player_only", "&cThis command can only be executed by a player!"),
    NO_PERMISSION("no_permission", "&cYou do not have permission to execute this command!"),
    PLAYER_HELP("player_help", String.join("\n",
            "&aPlayer commands:",
            "&e/discord link &a: link your Discord account",
            "&e/discord unlink &a: unlink your Discord account",
            "&e/discord claim &a: claim your Discord rewards"
    )),
    ADMIN_HELP("admin_help", String.join("\n",
            "&aAdmin commands:",
            "&e/discord reload &a: reload the plugin configuration"
    )),
    ERROR_OCCURRED("error_occurred", "&cAn error occurred while processing your request. Please contact support."),
    LINK_SUCCESS("link_success", "&aYour Discord account has been successfully linked!"),
    ALREADY_LINKED("already_linked", "&cYour Discord account is already linked!"),
    NOT_LINKED("not_linked", "&cYou have not linked your Discord account yet! Use &e/discord link &cto link it."),
    INVALID_CODE("invalid_code", "&cThe code you provided is invalid! Please try again."),
    CLAIM_SUCCESS("claim_success", "&aYou have successfully claimed your Discord rewards!"),
    CLAIM_NO_REWARD("claim_no_reward", "&cYou have no rewards to claim at this time."),
    PLUGIN_RELOADED("plugin_reloaded", "&ePlugin reloaded. Please check the console for any errors."),
    SYNC_ERROR_UNKNOWN_ROLE("sync_error_unknown_role", "&cAn error occurred while syncing roles. Unknown role detected. Please contact support.")
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
