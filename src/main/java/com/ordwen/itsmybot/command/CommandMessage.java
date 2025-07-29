package com.ordwen.itsmybot.command;

import com.ordwen.itsmybot.enumeration.Messages;
import org.bukkit.command.CommandSender;

public class CommandMessage {

    /**
     * Sends the player help message to the sender.
     *
     * @param sender the sender.
     */
    protected void help(CommandSender sender) {
        Messages.PLAYER_HELP.send(sender);
    }

    /**
     * Sends the no permission message to the sender.
     *
     * @param sender the sender.
     */
    protected void noPermission(CommandSender sender) {
        Messages.NO_PERMISSION.send(sender);
    }

    /**
     * Sends a message to the sender indicating that the command can only be executed by a player.
     *
     * @param sender the sender.
     */
    protected void playerOnly(CommandSender sender) {
        Messages.PLAYER_ONLY.send(sender);
    }
}
