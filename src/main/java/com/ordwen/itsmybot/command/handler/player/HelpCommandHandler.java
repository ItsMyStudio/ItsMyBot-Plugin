package com.ordwen.itsmybot.command.handler.player;

import com.ordwen.itsmybot.command.handler.CommandHandlerBase;
import com.ordwen.itsmybot.enumeration.Messages;
import com.ordwen.itsmybot.enumeration.Permissions;
import org.bukkit.entity.Player;

public class HelpCommandHandler extends CommandHandlerBase {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "itsmybot.command.help";
    }

    @Override
    public void execute(Player player, String[] args) {
        Messages.PLAYER_HELP.send(player);

        if (player.hasPermission(Permissions.RELOAD.get())) {
            Messages.ADMIN_HELP.send(player);
        }
    }
}
