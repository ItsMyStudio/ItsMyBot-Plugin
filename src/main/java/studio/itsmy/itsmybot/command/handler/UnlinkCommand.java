package studio.itsmy.itsmybot.command.handler;

import studio.itsmy.itsmybot.command.CommandMessage;
import studio.itsmy.itsmybot.command.handler.player.UnlinkCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnlinkCommand extends CommandMessage implements CommandExecutor {

    private final UnlinkCommandHandler handler;

    public UnlinkCommand(UnlinkCommandHandler handler) {
        this.handler = handler;
    }

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
