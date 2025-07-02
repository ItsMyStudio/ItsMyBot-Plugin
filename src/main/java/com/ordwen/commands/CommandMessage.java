package com.ordwen.commands;

import com.ordwen.enums.Messages;
import org.bukkit.command.CommandSender;

public class CommandMessage {

    /**
     * Sends the player help message to the sender.
     *
     * @param sender the sender.
     */
    protected void help(CommandSender sender) {
        final String msg = Messages.PLAYER_HELP.toString();
        if (msg != null) sender.sendMessage(msg);
    }

    /**
     * Sends the no permission message to the sender.
     *
     * @param sender the sender.
     */
    protected void noPermission(CommandSender sender) {
        final String msg = Messages.NO_PERMISSION.toString();
        if (msg != null) sender.sendMessage(msg);
    }

    /**
     * Sends a message to the sender indicating that the command can only be executed by a player.
     *
     * @param sender the sender.
     */
    protected void playerOnly(CommandSender sender) {
        final String msg = Messages.PLAYER_ONLY.toString();
        if (msg != null) sender.sendMessage(msg);
    }
}
