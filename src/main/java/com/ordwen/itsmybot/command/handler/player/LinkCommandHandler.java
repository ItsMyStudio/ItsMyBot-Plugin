package com.ordwen.itsmybot.command.handler.player;

import com.ordwen.itsmybot.ItsMyBotPlugin;
import com.ordwen.itsmybot.command.handler.CommandHandlerBase;
import com.ordwen.itsmybot.enumeration.Messages;
import com.ordwen.itsmybot.enumeration.Permissions;
import com.ordwen.itsmybot.ws.WSCommandExecutor;
import com.ordwen.itsmybot.ws.handler.command.LinkWSCommandHandler;
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
        if (args.length < 1 || args[0].trim().isEmpty()) {
            Messages.PLAYER_HELP.send(player);
            return;
        }

        executor.execute(player, args, new LinkWSCommandHandler());
    }
}
