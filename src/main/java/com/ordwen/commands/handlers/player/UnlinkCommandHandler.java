package com.ordwen.commands.handlers.player;

import com.ordwen.ItsMyBotPlugin;
import com.ordwen.commands.handlers.CommandHandlerBase;
import com.ordwen.enums.Messages;
import com.ordwen.enums.Permissions;
import com.ordwen.ws.WSCommandExecutor;
import com.ordwen.ws.handlers.UnlinkWSCommandHandler;
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
            player.sendMessage(Messages.PLAYER_HELP.toString());
            return;
        }

        executor.execute(player, args, new UnlinkWSCommandHandler());
    }
}
