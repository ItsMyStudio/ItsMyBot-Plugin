package com.ordwen;

import com.ordwen.commands.DiscordCommand;
import com.ordwen.commands.CommandRegistry;
import com.ordwen.commands.handlers.admin.ReloadCommandHandler;
import com.ordwen.commands.handlers.player.ClaimCommandHandler;
import com.ordwen.commands.handlers.player.LinkCommandHandler;
import com.ordwen.commands.handlers.player.UnlinkCommandHandler;
import com.ordwen.files.FilesManager;
import com.ordwen.listeners.PlayerJoinListener;
import com.ordwen.services.ReloadService;
import com.ordwen.utils.PluginLogger;
import com.ordwen.ws.WSClient;
import org.bukkit.plugin.java.JavaPlugin;

public class ItsMyBotPlugin extends JavaPlugin {

    private FilesManager filesManager;
    private ReloadService reloadService;

    private WSClient wsClient;

    @Override
    public void onEnable() {
        PluginLogger.info("Enabling plugin...");

        this.filesManager = new FilesManager(this);
        filesManager.load();

        this.reloadService = new ReloadService(this);
        reloadService.reload();

        final CommandRegistry commandRegistry = new CommandRegistry();
        commandRegistry.registerCommand(new LinkCommandHandler(this));
        commandRegistry.registerCommand(new UnlinkCommandHandler(this));
        commandRegistry.registerCommand(new ClaimCommandHandler(this));
        commandRegistry.registerCommand(new ReloadCommandHandler(reloadService));

        getCommand("discord").setExecutor(new DiscordCommand(commandRegistry));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        PluginLogger.info("Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        PluginLogger.info("Plugin is shutting down...");
        if (wsClient != null) {
            wsClient.disconnect();
            wsClient.shutdown();
        }
    }

    public void reloadWSClient() {
        if (wsClient == null) {
            wsClient = new WSClient();
        }

        wsClient.disconnect();
        wsClient.connect();
    }

    public WSClient getWSClient() {
        if (wsClient == null) {
            wsClient = new WSClient();
        }
        return wsClient;
    }

    public FilesManager getFilesManager() {
        return filesManager;
    }

    public ReloadService getReloadService() {
        return reloadService;
    }
}