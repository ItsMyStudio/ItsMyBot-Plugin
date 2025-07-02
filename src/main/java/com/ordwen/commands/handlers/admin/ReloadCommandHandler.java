package com.ordwen.commands.handlers.admin;

import com.ordwen.commands.handlers.CommandHandlerBase;
import com.ordwen.enums.Messages;
import com.ordwen.enums.Permissions;
import com.ordwen.services.ReloadService;
import org.bukkit.entity.Player;

public class ReloadCommandHandler extends CommandHandlerBase {

    private final ReloadService reloadService;

    public ReloadCommandHandler(ReloadService reloadService) {
        this.reloadService = reloadService;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return Permissions.RELOAD.get();
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 1) {
            player.sendMessage(Messages.ADMIN_HELP.toString());
            return;
        }

        reloadService.reload();
        player.sendMessage(Messages.PLUGIN_RELOADED.toString());
    }
}
