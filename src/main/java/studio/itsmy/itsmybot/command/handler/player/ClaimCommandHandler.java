package studio.itsmy.itsmybot.command.handler.player;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.command.handler.CommandHandlerBase;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.enumeration.Permissions;
import studio.itsmy.itsmybot.ws.WSCommandExecutor;
import studio.itsmy.itsmybot.ws.handler.command.ClaimWSCommandHandler;
import org.bukkit.entity.Player;

/**
 * Command handler for the {@code claim} subcommand.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Validates argument count (no extra arguments allowed).</li>
 *   <li>Delegates to {@link WSCommandExecutor} with a {@link ClaimWSCommandHandler} request.</li>
 *   <li>Sends {@link Messages#PLAYER_HELP} if usage is incorrect.</li>
 * </ul>
 * <p>
 * Triggers a WebSocket {@code CLAIM} request to fetch and execute pending reward actions for the player.
 */
public class ClaimCommandHandler extends CommandHandlerBase {

    private final WSCommandExecutor executor;

    /**
     * Creates a new claim handler.
     *
     * @param plugin main plugin instance (used to initialize the executor)
     */
    public ClaimCommandHandler(ItsMyBotPlugin plugin) {
        this.executor = new WSCommandExecutor(plugin);
    }

    @Override
    public String getName() {
        return "claim";
    }

    @Override
    public String getPermission() {
        return Permissions.CLAIM.get();
    }

    /**
     * Executes the claim subcommand.
     * <p>
     * If more than one argument is provided, displays help instead.
     */
    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 1) {
            Messages.PLAYER_HELP.send(player);
            return;
        }

        executor.execute(player, args, new ClaimWSCommandHandler());
    }
}
