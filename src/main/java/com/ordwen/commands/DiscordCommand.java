package com.ordwen.commands;

import com.ordwen.commands.handlers.CommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordCommand extends CommandMessage implements CommandExecutor {

    private final CommandRegistry commandRegistry;

    public DiscordCommand(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            playerOnly(sender);
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
