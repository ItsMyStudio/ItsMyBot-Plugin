package com.ordwen.listeners;

import com.ordwen.ItsMyBotPlugin;
import org.bukkit.event.Listener;

public class PlayerJoinListener implements Listener {

    private final ItsMyBotPlugin plugin;

    public PlayerJoinListener(ItsMyBotPlugin plugin) {
        this.plugin = plugin;
    }
}
