package studio.itsmy.itsmybot.command.handler.player;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.command.handler.CommandHandlerBase;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.enumeration.Permissions;
import studio.itsmy.itsmybot.ws.WSCommandExecutor;
import studio.itsmy.itsmybot.ws.handler.command.LinkWSCommandHandler;
import org.bukkit.entity.Player;

/**
 * Command handler for the {@code link} subcommand.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Validates that a link code is provided as the first argument.</li>
 *   <li>Delegates to {@link WSCommandExecutor} with a {@link LinkWSCommandHandler} request.</li>
 *   <li>Sends {@link Messages#CODE_REQUIRED} if no code is provided.</li>
 * </ul>
 * <p>
 * Initiates the WebSocket {@code LINK} flow to associate the player's account with Discord.
 */
public class LinkCommandHandler extends CommandHandlerBase {

    private final WSCommandExecutor executor;

    /**
     * Creates a new link handler.
     *
     * @param plugin main plugin instance (used to initialize the executor)
     */
    public LinkCommandHandler(ItsMyBotPlugin plugin) {
        this.executor = new WSCommandExecutor(plugin);
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public String getPermission() {
        return Permissions.LINK.get();
    }

    /**
     * Executes the link subcommand.
     * <p>
     * If no code is provided, displays a message explaining that a code is required.
     */
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1 || args[0].trim().isEmpty()) {
            Messages.CODE_REQUIRED.send(player);
            return;
        }

        executor.execute(player, args, new LinkWSCommandHandler());
    }
}
