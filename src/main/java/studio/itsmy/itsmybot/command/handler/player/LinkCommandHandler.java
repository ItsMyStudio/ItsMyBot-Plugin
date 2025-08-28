package studio.itsmy.itsmybot.command.handler.player;

import studio.itsmy.itsmybot.ItsMyBotPlugin;
import studio.itsmy.itsmybot.command.handler.CommandHandlerBase;
import studio.itsmy.itsmybot.enumeration.Messages;
import studio.itsmy.itsmybot.enumeration.Permissions;
import studio.itsmy.itsmybot.ws.WSCommandExecutor;
import studio.itsmy.itsmybot.ws.handler.command.LinkWSCommandHandler;
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
            Messages.CODE_REQUIRED.send(player);
            return;
        }

        executor.execute(player, args, new LinkWSCommandHandler());
    }
}
