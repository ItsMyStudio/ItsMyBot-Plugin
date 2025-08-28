package studio.itsmy.itsmybot.command.handler.player;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.command.handler.CommandHandlerBase;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.enumeration.Permissions;
import studio.itsmy.itsmybot.ws.WSCommandExecutor;
import studio.itsmy.itsmybot.ws.handler.command.ClaimWSCommandHandler;
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
