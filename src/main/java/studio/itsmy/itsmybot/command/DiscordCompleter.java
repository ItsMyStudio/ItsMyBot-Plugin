package studio.itsmy.itsmybot.command;

import studio.itsmy.itsmybot.command.handler.CommandHandlerBase;
import studio.itsmy.itsmybot.enumeration.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscordCompleter implements TabCompleter {

    private final CommandRegistry commandRegistry;

    public DiscordCompleter(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            final List<String> suggestions = new ArrayList<>();

            if (sender.hasPermission(Permissions.RELOAD.get())) {
                suggestions.add("reload");
            }

            for (CommandHandlerBase cmd : commandRegistry.getCommandHandlers()) {
                if (!sender.hasPermission(cmd.getPermission())) continue;
                suggestions.add(cmd.getName());
            }

            return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        } else {
            final CommandHandlerBase subCommand = commandRegistry.getCommandHandler(args[0]);
            if (subCommand == null) return Collections.emptyList();
            return subCommand.onTabComplete(sender, args);
        }
    }
}
