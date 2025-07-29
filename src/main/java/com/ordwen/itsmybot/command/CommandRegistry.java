package com.ordwen.itsmybot.command;

import com.ordwen.itsmybot.command.handler.CommandHandlerBase;

import java.util.*;

/**
 * Registry for all player subcommands in the plugin.
 * <p>
 * This class uses {@link HashMap}, where each entry maps a subcommand name to its corresponding {@link CommandHandlerBase} handler.
 * It provides utility methods to register and retrieve player commands.
 * <p>
 * Addons can use this registry to register their own player commands.
 */
public class CommandRegistry {

    private final Map<String, CommandHandlerBase> handlers = new HashMap<>();

    /**
     * Registers a new player subcommand handler.
     * <p>
     * The handler is stored using its {@link CommandHandlerBase#getName()} as the key.
     *
     * @param handler the player command handler to register
     */
    public void registerCommand(CommandHandlerBase handler) {
        final String name = handler.getName().toLowerCase();
        handlers.put(name, handler);
    }

    /**
     * Retrieves the command handler associated with the given name.
     *
     * @param name the name of the subcommand
     * @return the corresponding {@link CommandHandlerBase}, or {@code null} if not found
     */
    public CommandHandlerBase getCommandHandler(String name) {
        final String key = name.toLowerCase();
        return handlers.get(key);
    }

    /**
     * Gets a collection of all registered player command handlers.
     *
     * @return a collection of subcommand names to their handlers
     */
    public Collection<CommandHandlerBase> getCommandHandlers() {
        return handlers.values();
    }
}
