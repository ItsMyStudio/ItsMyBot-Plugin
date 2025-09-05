package studio.itsmy.itsmybot.command.handler;

import studio.itsmy.itsmybot.command.CommandMessage;
import studio.itsmy.itsmybot.command.handler.player.UnlinkCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command executor for the {@code /unlink} command.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Ensures the sender is a player (console is not allowed).</li>
 *   <li>Checks the player's permission from {@link UnlinkCommandHandler#getPermission()}.</li>
 *   <li>Delegates execution to {@link UnlinkCommandHandler} if allowed.</li>
 *   <li>Sends appropriate "no permission" or "player only" messages otherwise.</li>
 * </ul>
 * <p>
 * Will trigger a WebSocket UNLINK request if the player is permitted.
 */
public class UnlinkCommand extends CommandMessage implements CommandExecutor {

    private final UnlinkCommandHandler handler;

    /**
     * Creates a new {@code /unlink} command executor.
     *
     * @param handler the handler responsible for building and sending the unlink request
     */
    public UnlinkCommand(UnlinkCommandHandler handler) {
        this.handler = handler;
    }

    /**
     * Executes the {@code /unlink} command.
     *
     * @param sender  the command sender
     * @param command the command
     * @param label   the alias used
     * @param args    command arguments (ignored for this command)
     * @return always {@code true} to prevent default usage message
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            playerOnly(sender);
            return true;
        }

        final Player player = (Player) sender;
        if (player.hasPermission(handler.getPermission())) {
            handler.execute(player, args);
        } else {
            noPermission(player);
        }
        return true;
    }
}
