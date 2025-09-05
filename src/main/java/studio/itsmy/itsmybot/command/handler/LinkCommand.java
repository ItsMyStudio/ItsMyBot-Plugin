package studio.itsmy.itsmybot.command.handler;

import studio.itsmy.itsmybot.command.CommandMessage;
import studio.itsmy.itsmybot.command.handler.player.LinkCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command executor for the {@code /link} command.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Ensures the sender is a player (console is not allowed).</li>
 *   <li>Checks the player's permission from {@link LinkCommandHandler#getPermission()}.</li>
 *   <li>Delegates execution to {@link LinkCommandHandler} if allowed.</li>
 *   <li>Sends appropriate "no permission" or "player only" messages otherwise.</li>
 * </ul>
 * <p>
 * Will send a WebSocket LINK request containing the player's UUID, name, and the provided code.
 */
public class LinkCommand extends CommandMessage implements CommandExecutor {

    private final LinkCommandHandler handler;

    /**
     * Creates a new {@code /link} command executor.
     *
     * @param handler the handler responsible for building and sending the link request
     */
    public LinkCommand(LinkCommandHandler handler) {
        this.handler = handler;
    }

    /**
     * Executes the {@code /link} command.
     *
     * @param sender  the command sender
     * @param command the command
     * @param label   the alias used
     * @param args    command arguments (expects a link code as the first argument)
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