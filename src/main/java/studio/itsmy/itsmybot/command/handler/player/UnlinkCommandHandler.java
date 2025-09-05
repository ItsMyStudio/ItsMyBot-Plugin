package studio.itsmy.itsmybot.command.handler.player;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.command.handler.CommandHandlerBase;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.enumeration.Permissions;
import studio.itsmy.itsmybot.ws.WSCommandExecutor;
import studio.itsmy.itsmybot.ws.handler.command.UnlinkWSCommandHandler;
import org.bukkit.entity.Player;

/**
 * Command handler for the {@code unlink} subcommand.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Validates argument count (no extra arguments allowed).</li>
 *   <li>Delegates to {@link WSCommandExecutor} with a {@link UnlinkWSCommandHandler} request.</li>
 *   <li>Sends {@link Messages#PLAYER_HELP} if usage is incorrect.</li>
 * </ul>
 * <p>
 * Triggers a WebSocket {@code UNLINK} request that removes the link between the player and their Discord account.
 */
public class UnlinkCommandHandler extends CommandHandlerBase {

    private final WSCommandExecutor executor;

    /**
     * Creates a new unlink handler.
     *
     * @param plugin main plugin instance (used to initialize the executor)
     */
    public UnlinkCommandHandler(ItsMyBotPlugin plugin) {
        this.executor = new WSCommandExecutor(plugin);
    }

    @Override
    public String getName() {
        return "unlink";
    }

    @Override
    public String getPermission() {
        return Permissions.UNLINK.get();
    }

    /**
     * Executes the unlink subcommand.
     * <p>
     * If more than one argument is provided, displays player help instead.
     */
    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 1) {
            Messages.PLAYER_HELP.send(player);
            return;
        }

        executor.execute(player, args, new UnlinkWSCommandHandler());
    }
}
