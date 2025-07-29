package com.ordwen.command;

import com.ordwen.command.handler.CommandHandler;
import com.ordwen.enumeration.Messages;
import com.ordwen.enumeration.Permissions;
import com.ordwen.service.ReloadService;
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
        if (!(sender instanceof Player)) {
            if (args.length >= 1 && "reload".equalsIgnoreCase(args[0])) {
                if (sender.hasPermission(Permissions.RELOAD.get())) {
                    reloadService.reload();
                    sender.sendMessage(Messages.PLUGIN_RELOADED.toString());
                } else {
                    noPermission(sender);
                }
            } else {
                playerOnly(sender);
            }
            return true;
        }

        final Player player = (Player) sender;

        if (args.length >= 1) {
            final CommandHandler handler = commandRegistry.getCommandHandler(args[0]);
            if (handler != null) {
                if (player.hasPermission(handler.getPermission())) {
                    handler.execute(player, args);
                    return true;
                } else {
                    noPermission(player);
                }
            }
        }

        help(sender);
        return true;
    }
}
