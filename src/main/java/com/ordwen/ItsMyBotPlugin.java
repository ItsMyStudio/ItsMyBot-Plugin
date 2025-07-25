package com.ordwen;

import com.ordwen.command.DiscordCommand;
import com.ordwen.command.CommandRegistry;
import com.ordwen.command.handler.admin.ReloadCommandHandler;
import com.ordwen.command.handler.player.ClaimCommandHandler;
import com.ordwen.command.handler.player.LinkCommandHandler;
import com.ordwen.command.handler.player.UnlinkCommandHandler;
import com.ordwen.file.FilesManager;
import com.ordwen.listener.PlayerCommandListener;
import com.ordwen.listener.PlayerJoinListener;
import com.ordwen.listener.PlayerQuitListener;
import com.ordwen.service.LogService;
import com.ordwen.service.ReloadService;
import com.ordwen.util.PluginLogger;
import com.ordwen.ws.handler.role.RoleSyncUtil;
import com.ordwen.ws.WSClient;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ItsMyBotPlugin extends JavaPlugin {

    private FilesManager filesManager;
    private ReloadService reloadService;
    private LogService logService;

    private Permission permission;
    private WSClient wsClient;

    private AutoCloseable nodeMutateSubscription;

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

        rgisterListeners();
        hookLuckPerms();

        this.logService = new LogService(this);
        logService.logServerStart();

        PluginLogger.info("Plugin has been enabled!");
    }

    private void rgisterListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(this), this);
    }

    private void hookLuckPerms() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            final RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider == null) {
                PluginLogger.error("LuckPerms provider not found! Please ensure LuckPerms is working correctly.");
                return;
            }

            final LuckPerms luckPerms = provider.getProvider();
            if (luckPerms == null) {
                PluginLogger.error("LuckPerms provider is null! Please ensure LuckPerms is working correctly.");
                return;
            }

            final EventBus eventBus = luckPerms.getEventBus();
            this.nodeMutateSubscription = eventBus.subscribe(this, NodeMutateEvent.class, event -> {
                final Player player = getServer().getPlayer(event.getTarget().getFriendlyName());
                if (player != null && player.isOnline()) {
                    RoleSyncUtil.sendRoleSyncUpdate(this, player);
                }
            });
        } else {
            PluginLogger.info("LuckPerms plugin is missing! Server changes will not be synced with Discord until the next player joins.");
        }
    }

    @Override
    public void onDisable() {
        PluginLogger.info("Plugin is shutting down...");
        logService.logServerStop();

        if (nodeMutateSubscription != null) {
            try {
                nodeMutateSubscription.close();
            } catch (Exception e) {
                PluginLogger.error("Failed to unsubscribe from LuckPerms events" + e.getMessage());
            }
        }

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
        getServer().getScheduler().runTaskLater(this, () -> wsClient.connect(), 40L);
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

    public LogService getLogService() {
        return logService;
    }

    public Permission getPermission() {
        return permission;
    }
}