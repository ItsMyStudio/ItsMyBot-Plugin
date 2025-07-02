package com.ordwen.commands.handlers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Represents a player subcommand that can be executed through the plugin's player command system.
 * <p>
 * Implementing classes must define the command logic, the name of the subcommand, and the required permission.
 * These commands are intended to be used by players only (not the console), and are typically registered
 * in the {@code PlayerCommandRegistry}.
 */
public interface CommandHandler {

    /**
     * Gets the name of the command. This is used as the subcommand identifier (e.g., {@code /command <name>}).
     *
     * @return the subcommand name
     */
    String getName();

    /**
     * Gets the required permission to execute the command.
     *
     * @return the permission node as a string
     */
    String getPermission();

    /**
     * Executes the command logic when the command is invoked by a player.
     *
     * @param player the player executing the command
     * @param args   the arguments passed to the command
     */
    void execute(Player player, String[] args);

    /**
     * Provides tab-completion suggestions for the command.
     * <p>
     * This method is invoked by Bukkit when the user presses the <kbd>TAB</kbd> key while typing the command.
     * It allows you to offer context-aware completions, such as subcommands, player names, or other dynamic options.
     * <p>
     * Returning {@code null} will let Bukkit suggest player names automatically.
     * Returning an empty list disables tab-completion suggestions for the current argument.
     *
     * @param sender the command sender (can be a player or the console)
     * @param args   the current arguments entered for the command
     * @return a list of possible completions, or an empty list if no completions are available, or {@code null} to allow Bukkit to automatically complete player names
     */
    List<String> onTabComplete(CommandSender sender, String[] args);
}
