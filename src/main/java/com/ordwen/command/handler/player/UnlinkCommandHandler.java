package com.ordwen.command.handler.player;

import com.ordwen.ItsMyBotPlugin;
import com.ordwen.command.handler.CommandHandlerBase;
import com.ordwen.enumeration.Messages;
import com.ordwen.enumeration.Permissions;
import com.ordwen.ws.WSCommandExecutor;
import com.ordwen.ws.handler.command.UnlinkWSCommandHandler;
import org.bukkit.entity.Player;

public class UnlinkCommandHandler extends CommandHandlerBase {

    private final WSCommandExecutor executor;

    public UnlinkCommandHandler(ItsMyBotPlugin plugin) {
        this.executor = new WSCommandExecutor(plugin);
    }

    @Override
    public String getName() {
        return "unlink";
    }

    @Override
    public String getPermission() {
        return Permissions.UNLINK.get();
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 1) {
            Messages.PLAYER_HELP.send(player);
            return;
        }

        executor.execute(player, args, new UnlinkWSCommandHandler());
    }
}
