package studio.itsmy.itsmybot.command;

import studio.itsmy.itsmybot.command.handler.CommandHandler;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.enumeration.Permissions;
import studio.itsmy.itsmybot.service.ReloadService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Primary executor for the {@code /discord} command.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Permission-checking for {@link Permissions#USE} and subcommands.</li>
 *   <li>Handles special {@code reload} subcommand (requires {@link Permissions#RELOAD}).</li>
 *   <li>Delegates to {@link CommandRegistry} for custom subcommands.</li>
 *   <li>Displays {@link Messages#JOIN_DISCORD} if no arguments are provided.</li>
 * </ul>
 */
public class DiscordCommand extends CommandMessage implements CommandExecutor {

    private final CommandRegistry commandRegistry;
    private final ReloadService reloadService;

    /**
     * Creates a new {@code /discord} executor.
     *
     * @param commandRegistry registry for resolving subcommands
     * @param reloadService   reload service for live reloading
     */
    public DiscordCommand(CommandRegistry commandRegistry, ReloadService reloadService) {
        this.commandRegistry = commandRegistry;
        this.reloadService = reloadService;
    }

    /**
     * Handles command execution for {@code /discord}.
     *
     * @param sender sender of the command (console or player)
     * @param command the command
     * @param label   alias used
     * @param args    command arguments
     * @return always {@code true} to prevent default usage message
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(Permissions.USE.get())) {
            noPermission(sender);
            return true;
        }

        // Handle reload command
        if (args.length >= 1 && "reload".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission(Permissions.RELOAD.get())) {
                reloadService.reload();
                Messages.PLUGIN_RELOADED.send(sender);
            } else {
                noPermission(sender);
            }
            return true;
        }

        // Only players may use other subcommands
        if (!(sender instanceof Player)) {
            playerOnly(sender);
            return true;
        }

        final Player player = (Player) sender;

        // No arguments: show invitation message
        if (args.length == 0) {
            Messages.JOIN_DISCORD.send(player);
            return true;
        }

        // Resolve subcommand and execute if permitted
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
