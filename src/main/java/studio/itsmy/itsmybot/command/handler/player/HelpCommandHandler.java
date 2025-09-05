package studio.itsmy.itsmybot.command.handler.player;

import studio.itsmy.itsmybot.command.handler.CommandHandlerBase;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.enumeration.Permissions;
import org.bukkit.entity.Player;

/**
 * Command handler for the {@code help} subcommand.
 * <p>
 * Displays a formatted help message for available commands, and shows additional
 * admin commands if the player has {@link Permissions#RELOAD}.
 * <p>
 * Displays a list of available commands and their descriptions.
 */
public class HelpCommandHandler extends CommandHandlerBase {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "itsmybot.command.help";
    }

    /**
     * Sends help messages to the player, including admin help if permitted.
     */
    @Override
    public void execute(Player player, String[] args) {
        Messages.PLAYER_HELP.send(player);

        if (player.hasPermission(Permissions.RELOAD.get())) {
            Messages.ADMIN_HELP.send(player);
        }
    }
}
