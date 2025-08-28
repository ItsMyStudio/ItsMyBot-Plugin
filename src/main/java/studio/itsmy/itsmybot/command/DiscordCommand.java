package studio.itsmy.itsmybot.command;

import studio.itsmy.itsmybot.command.handler.CommandHandler;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.enumeration.Permissions;
import studio.itsmy.itsmybot.service.ReloadService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordCommand extends CommandMessage implements CommandExecutor {

    private final CommandRegistry commandRegistry;
    private final ReloadService reloadService;

    public DiscordCommand(CommandRegistry commandRegistry, ReloadService reloadService) {
        this.commandRegistry = commandRegistry;
        this.reloadService = reloadService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(Permissions.USE.get())) {
            noPermission(sender);
            return true;
        }

        if (args.length >= 1 && "reload".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission(Permissions.RELOAD.get())) {
                reloadService.reload();
                Messages.PLUGIN_RELOADED.send(sender);
            } else {
                noPermission(sender);
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            playerOnly(sender);
            return true;
        }

        final Player player = (Player) sender;

        if (args.length == 0) {
            Messages.JOIN_DISCORD.send(player);
            return true;
        }

        final CommandHandler handler = commandRegistry.getCommandHandler(args[0]);
        if (handler != null) {
            if (player.hasPermission(handler.getPermission())) {
                handler.execute(player, args);
                return true;
            } else {
                noPermission(player);
            }
        }

        return true;
    }
}
