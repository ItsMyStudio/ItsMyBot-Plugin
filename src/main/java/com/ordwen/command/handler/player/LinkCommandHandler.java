package com.ordwen.command.handler.player;

import com.ordwen.ItsMyBotPlugin;
import com.ordwen.command.handler.CommandHandlerBase;
import com.ordwen.enumeration.Messages;
import com.ordwen.enumeration.Permissions;
import com.ordwen.ws.WSCommandExecutor;
import com.ordwen.ws.handler.command.LinkWSCommandHandler;
import org.bukkit.entity.Player;

public class LinkCommandHandler extends CommandHandlerBase {

    private final WSCommandExecutor executor;

    public LinkCommandHandler(ItsMyBotPlugin plugin) {
        this.executor = new WSCommandExecutor(plugin);
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public String getPermission() {
        return Permissions.LINK.get();
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2 || args[1].trim().isEmpty()) {
            player.sendMessage(Messages.PLAYER_HELP.toString());
            return;
        }

        executor.execute(player, args, new LinkWSCommandHandler());
    }
}
