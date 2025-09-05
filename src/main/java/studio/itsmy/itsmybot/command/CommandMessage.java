package studio.itsmy.itsmybot.command;

import studio.itsmy.itsmybot.enumeration.Messages;
import org.bukkit.command.CommandSender;

/**
 * Base class providing utility methods for sending common command feedback messages.
 * <p>
 * Used by {@link DiscordCommand} and potentially other command executors.
 */
public class CommandMessage {

    /**
     * Sends a standard "no permission" message to the sender.
     *
     * @param sender command sender
     */
    protected void noPermission(CommandSender sender) {
        Messages.NO_PERMISSION.send(sender);
    }

    /**
     * Sends a message indicating that the command can only be run by a player (not console).
     *
     * @param sender command sender
     */
    protected void playerOnly(CommandSender sender) {
        Messages.PLAYER_ONLY.send(sender);
    }
}
