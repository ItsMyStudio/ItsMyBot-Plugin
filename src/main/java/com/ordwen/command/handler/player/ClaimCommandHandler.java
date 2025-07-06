package com.ordwen.command.handler.player;

import com.ordwen.ItsMyBotPlugin;
import com.ordwen.command.handler.CommandHandlerBase;
import com.ordwen.enumeration.Messages;
import com.ordwen.enumeration.Permissions;
import com.ordwen.ws.WSCommandExecutor;
import com.ordwen.ws.handler.command.ClaimWSCommandHandler;
import org.bukkit.entity.Player;

public class ClaimCommandHandler extends CommandHandlerBase{

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
            player.sendMessage(Messages.PLAYER_HELP.toString());
            return;
        }

        executor.execute(player, args, new ClaimWSCommandHandler());
    }
}
