package com.ordwen.commands.handlers.player;

import com.ordwen.ItsMyBotPlugin;
import com.ordwen.commands.handlers.CommandHandlerBase;
import com.ordwen.enums.Messages;
import com.ordwen.enums.Permissions;
import com.ordwen.ws.WSCommandExecutor;
import com.ordwen.ws.handlers.LinkWSCommandHandler;
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
