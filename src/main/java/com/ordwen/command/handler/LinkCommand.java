package com.ordwen.command.handler;

import com.ordwen.command.CommandMessage;
import com.ordwen.command.handler.player.LinkCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinkCommand extends CommandMessage implements CommandExecutor {

    private final LinkCommandHandler handler;

    public LinkCommand(LinkCommandHandler handler) {
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