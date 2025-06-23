package com.ordwen;

import com.ordwen.commands.ReloadService;
import com.ordwen.utils.PluginLogger;
import com.ordwen.ws.PluginClient;
import org.bukkit.plugin.java.JavaPlugin;

public class ItsMyBotPlugin extends JavaPlugin {

    private ReloadService reloadService;

    @Override
    public void onEnable() {
        PluginLogger.info("Enabling plugin...");
        saveDefaultConfig();

        this.reloadService = new ReloadService(this);
        reloadService.reload();

        final PluginClient client = new PluginClient();
        client.connect();

        PluginLogger.info("Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        PluginLogger.info("Plugin is shutting down...");
    }

    public ReloadService getReloadService() {
        return reloadService;
    }
}