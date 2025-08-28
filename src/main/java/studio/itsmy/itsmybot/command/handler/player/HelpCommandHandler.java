package studio.itsmy.itsmybot.command.handler.player;

import studio.itsmy.itsmybot.command.handler.CommandHandlerBase;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.enumeration.Permissions;
import org.bukkit.entity.Player;

public class HelpCommandHandler extends CommandHandlerBase {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "itsmybot.command.help";
    }

    @Override
    public void execute(Player player, String[] args) {
        Messages.PLAYER_HELP.send(player);

        if (player.hasPermission(Permissions.RELOAD.get())) {
            Messages.ADMIN_HELP.send(player);
        }
    }
}
