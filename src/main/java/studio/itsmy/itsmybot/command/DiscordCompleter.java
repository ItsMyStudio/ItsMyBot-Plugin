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

/**
 * Tab completer for the main {@code /discord} command.
 * <p>
 * Provides dynamic suggestions based on:
 * <ul>
 *   <li>{@code reload} subcommand if the sender has {@link Permissions#RELOAD}</li>
 *   <li>Registered {@link CommandHandlerBase} instances in {@link CommandRegistry}</li>
 * </ul>
 * and filters them by sender permissions.
 *
 * <h2>Examples</h2>
 * <pre>
 * /discord &lt;tab&gt; → reload | link | unlink | claim (if permitted)
 * </pre>
 */
public class DiscordCompleter implements TabCompleter {

    private final CommandRegistry commandRegistry;

    /**
     * Creates a new completer.
     *
     * @param commandRegistry registry used to look up available subcommands
     */
    public DiscordCompleter(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    /**
     * Provides completion suggestions for {@code /discord}.
     * <p>
     * - If {@code args.length == 1}, suggests top-level subcommands.<br>
     * - Otherwise delegates to the subcommand's {@link CommandHandlerBase#onTabComplete(CommandSender, String[])}
     *   if it exists.
     *
     * @return list of suggestions (possibly empty)
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            final List<String> suggestions = new ArrayList<>();

            // Include reload if permitted
            if (sender.hasPermission(Permissions.RELOAD.get())) {
                suggestions.add("reload");
            }

            // Add subcommands from registry if sender has permission
            for (CommandHandlerBase cmd : commandRegistry.getCommandHandlers()) {
                if (!sender.hasPermission(cmd.getPermission())) continue;
                suggestions.add(cmd.getName());
            }

            // Partial match filter
            return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        } else {
            final CommandHandlerBase subCommand = commandRegistry.getCommandHandler(args[0]);
            if (subCommand == null) return Collections.emptyList();
            return subCommand.onTabComplete(sender, args);
        }
    }
}
