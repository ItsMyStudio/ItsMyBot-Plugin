package com.ordwen;

import com.ordwen.command.DiscordCommand;
import com.ordwen.command.CommandRegistry;
import com.ordwen.command.handler.admin.ReloadCommandHandler;
import com.ordwen.command.handler.player.ClaimCommandHandler;
import com.ordwen.command.handler.player.LinkCommandHandler;
import com.ordwen.command.handler.player.UnlinkCommandHandler;
import com.ordwen.file.FilesManager;
import com.ordwen.listener.PlayerJoinListener;
import com.ordwen.service.ReloadService;
import com.ordwen.util.PluginLogger;
import com.ordwen.ws.WSClient;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public class ItsMyBotPlugin extends JavaPlugin {

    private FilesManager filesManager;
    private ReloadService reloadService;

    private Permission permission;
    private WSClient wsClient;

    @Override
    public void onEnable() {
        PluginLogger.info("Enabling plugin...");

        this.filesManager = new FilesManager(this);
        filesManager.load();

        this.reloadService = new ReloadService(this);
        reloadService.reload();

        setupPermissions();

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
            wsClient = new WSClient(this);
        }

        wsClient.disconnect();
        wsClient.connect();
    }

    public WSClient getWSClient() {
        if (wsClient == null) {
            wsClient = new WSClient(this);
        }
        return wsClient;
    }

    private void setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            permission = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
            if (permission == null) {
                PluginLogger.error("Vault permission provider not found!");
            }
        } else {
            PluginLogger.info("Vault plugin not found! Groups sync will not work.");
        }
    }

    public FilesManager getFilesManager() {
        return filesManager;
    }

    public ReloadService getReloadService() {
        return reloadService;
    }

    public Permission getPermission() {
        return permission;
    }
}