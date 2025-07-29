package com.ordwen.itsmybot.command.handler.player;

import com.ordwen.itsmybot.ItsMyBotPlugin;
import com.ordwen.itsmybot.command.handler.CommandHandlerBase;
import com.ordwen.itsmybot.enumeration.Messages;
import com.ordwen.itsmybot.enumeration.Permissions;
import com.ordwen.itsmybot.ws.WSCommandExecutor;
import com.ordwen.itsmybot.ws.handler.command.ClaimWSCommandHandler;
import org.bukkit.entity.Player;

public class ClaimCommandHandler extends CommandHandlerBase {

    private final WSCommandExecutor executor;

    public ClaimCommandHandler(ItsMyBotPlugin plugin) {
        this.executor = new WSCommandExecutor(plugin);
    }

    @Override
    public String getName() {
        return "claim";
    }

    @Override
    public String getPermission() {
        return Permissions.CLAIM.get();
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 1) {
            Messages.PLAYER_HELP.send(player);
            return;
        }

        executor.execute(player, args, new ClaimWSCommandHandler());
    }
}
