package com.ordwen.itsmybot.enumeration;

import com.ordwen.itsmybot.configuration.essential.Prefix;
import com.ordwen.itsmybot.file.implementation.MessagesFile;
import com.ordwen.itsmybot.util.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum Messages {

    PLAYER_ONLY("player_only", "<red>This command can only be executed by a player!</red>"),
    NO_PERMISSION("no_permission", "<red>You do not have permission to execute this command!</red>"),
    PLAYER_HELP("player_help", String.join("\n",
            "<green>Player commands:</green>",
            "<yellow>/link <green>: link your Discord account</green>",
            "<yellow>/unlink <green>: unlink your Discord account</green>",
            "<yellow>/discord claim <green>: claim your Discord rewards</green>"
    )),
    ADMIN_HELP("admin_help", String.join("\n",
            "<green>Admin commands:</green>",
            "<yellow>/discord reload <green>: reload the plugin configuration</green>"
    )),
    ERROR_OCCURRED("error_occurred", "<red>An error occurred while processing your request. Please contact support.</red>"),
    LINK_SUCCESS("link_success", "<green>Your Discord account has been successfully linked!</green>"),
    UNLINK_SUCCESS("unlink_success", "<green>Your Discord account has been successfully unlinked!</green>"),
    ALREADY_LINKED("already_linked", "<red>Your Discord account is already linked!</red>"),
    NOT_LINKED("not_linked", "<red>You have not linked your Discord account yet!</red> <yellow>Use</yellow> <yellow>/link</yellow> <red>to link it.</red>"),
    INVALID_CODE("invalid_code", "<red>The code you provided is invalid! Please try again.</red>"),
    CLAIM_SUCCESS("claim_success", "<green>You have successfully claimed your Discord rewards!</green>"),
    CLAIM_NO_REWARD("claim_no_reward", "<red>You have no rewards to claim at this time.</red>"),
    PLUGIN_RELOADED("plugin_reloaded", "<yellow>Plugin reloaded. Please check the console for any errors.</yellow>")

    ;

    private final String path;
    private final String defaultMessage;

    Messages(String path, String message) {
        this.path = path;
        this.defaultMessage = message;
    }

    public void send(CommandSender sender) {
        String msg = MessagesFile.getInstance().get(this.path, defaultMessage);

        if (msg != null && !msg.trim().isEmpty()) {
            if (sender instanceof Player) {
                final Player player = (Player) sender;
                TextFormatter.send(player, Prefix.getPrefix() + msg);
            } else {
                Bukkit.getConsoleSender().sendMessage(TextFormatter.legacy(Prefix.getPrefix() + msg));
            }
        }
    }

    public void send(Player player) {
        String msg = MessagesFile.getInstance().get(this.path, defaultMessage);

        if (!msg.trim().isEmpty()) {
            TextFormatter.send(player, Prefix.getPrefix() + msg);
        }
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
