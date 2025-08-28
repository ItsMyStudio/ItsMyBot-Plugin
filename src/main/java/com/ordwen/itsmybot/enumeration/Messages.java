package com.ordwen.itsmybot.enumeration;

import com.ordwen.itsmybot.file.implementation.MessagesFile;
import com.ordwen.itsmybot.util.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum Messages {

    PLAYER_ONLY("player_only", "<prefix> <#E0E3FF>This command can only be executed by a player!</#E0E3FF>"),
    NO_PERMISSION("no_permission", "<prefix> <#E0E3FF>You do not have permission to execute this command!</#E0E3FF>"),
    PLAYER_HELP("player_help", String.join("\n",
            "<prefix> <#E0E3FF>ᴄᴏᴍᴍᴀɴᴅꜱ</#E0E3FF><br>",
            "<#E0E3FF> • <#5865F2>/discord</#5865F2>: show the Discord link</#E0E3FF>",
            "<#E0E3FF> • <#5865F2>/link</#5865F2>: link your Discord account</#E0E3FF>",
            "<#E0E3FF> • <#5865F2>/unlink</#5865F2>: unlink your Discord account</#E0E3FF>",
            "<#E0E3FF> • <#5865F2>/discord claim</#5865F2>: claim your Discord rewards</#E0E3FF>"
    )),
    ADMIN_HELP("admin_help", String.join("\n",
            "<br><#E0E3FF> • <#5865F2>/discord reload</#5865F2>: reload the plugin configuration</#E0E3FF>",
            "<br><#E0E3FF> • <#5865F2>ᴘʀᴏᴊᴇᴛ</#5865F2>: ItsMyStudio",
            "<#E0E3FF> • <#5865F2>sᴜᴘᴘᴏʀᴛ</#5865F2>: <click:open_url:'https://itsmy.studio/discord'>itsmy.studio/discord</click>",
            "<#E0E3FF> • <#5865F2>ᴅᴇᴠᴇʟᴏᴘᴇʀ</#5865F2>: <hover:show_text:'Discord: <#5865F2>@ordwen</#5865F2><br>GitHub: <#5865F2>github.com/Ordwen</#5865F2>'><click:open_url:'https://github.com/Ordwen'>Ordwen</click></hover>"
    )),
    ERROR_OCCURRED("error_occurred", "<prefix> <#E0E3FF>An error occurred while processing your request. Please contact support.</#E0E3FF>"),
    BOT_NOT_CONNECTED("bot_not_connected", "<prefix> <#E0E3FF>The bot is not connected to Discord! Please inform an administrator.</#E0E3FF>"),
    LINK_SUCCESS("link_success", "<prefix> <#E0E3FF>Your Discord account has been successfully linked!</#E0E3FF>"),
    UNLINK_SUCCESS("unlink_success", "<prefix> <#E0E3FF>Your Discord account has been successfully unlinked!</#E0E3FF>"),
    ALREADY_LINKED("already_linked", "<prefix> <#E0E3FF>Your Discord account is already linked!</#E0E3FF>"),
    NOT_LINKED("not_linked", "<prefix> <#E0E3FF>You have not linked your Discord account yet! Use /link to link your account.</#E0E3FF>"),
    CODE_REQUIRED("code_required", "<prefix> <#E0E3FF>To link your Discord account, join the <click:\"open_url\":\"https://discord.gg/yourserver\"><#5865F2>ᴅɪꜱᴄᴏʀᴅ</#5865F2></click>, run the /link command there, and follow the given steps.</#E0E3FF>"),
    INVALID_CODE("invalid_code", "<prefix> <#E0E3FF>The code you provided is invalid! Please try again.</#E0E3FF>"),
    CLAIM_SUCCESS("claim_success", "<prefix> <#E0E3FF>You have successfully claimed your rewards!</#E0E3FF>"),
    CLAIM_NO_REWARD("claim_no_reward", "<prefix> <#E0E3FF>You have no rewards to claim at this time.</#E0E3FF>"),
    PLUGIN_RELOADED("plugin_reloaded", "<prefix> <#E0E3FF>Plugin reloaded. Please check the console for any errors.</#E0E3FF>"),
    JOIN_DISCORD("join_discord", "<prefix> <#E0E3FF>Join our Discord to stay updated and link your account!</#E0E3FF> <click:\"open_url\":\"https://discord.gg/yourserver\"><#5865F2>ᴄʟɪᴄᴋ ᴛᴏ ᴊᴏɪɴ →</#5865F2></click>")

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
                TextFormatter.send(player, msg);
            } else {
                Bukkit.getConsoleSender().sendMessage(TextFormatter.legacy(msg.replace("<prefix>", "[ItsMyBot] ")));
            }
        }
    }

    public void send(Player player) {
        String msg = MessagesFile.getInstance().get(this.path, defaultMessage);

        if (!msg.trim().isEmpty()) {
            TextFormatter.send(player, msg);
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
