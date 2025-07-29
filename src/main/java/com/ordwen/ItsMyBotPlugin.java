package com.ordwen;

import com.ordwen.command.DiscordCommand;
import com.ordwen.command.CommandRegistry;
import com.ordwen.command.DiscordCompleter;
import com.ordwen.command.handler.LinkCommand;
import com.ordwen.command.handler.UnlinkCommand;
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
import com.ordwen.util.TextFormatter;
import com.ordwen.ws.handler.role.LuckPermsSyncManager;
import com.ordwen.ws.WSClient;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ItsMyBotPlugin extends JavaPlugin {

    private LogService logService;

    private Permission permission;
    private WSClient wsClient;

    private LuckPermsSyncManager lpSyncManager;

    @Override
    public void onEnable() {
        PluginLogger.info("Enabling plugin...");

        final BukkitAudiences audiences = BukkitAudiences.create(this);
        TextFormatter.init(audiences);

        final FilesManager filesManager = new FilesManager(this);
        filesManager.load();

        final ReloadService reloadService = new ReloadService(this);
        reloadService.reload();

        setupPermissions();

        final CommandRegistry commandRegistry = new CommandRegistry();
        commandRegistry.registerCommand(new ClaimCommandHandler(this));

        final LinkCommandHandler linkCommandHandler = new LinkCommandHandler(this);
        getCommand("link").setExecutor(new LinkCommand(linkCommandHandler));

        final UnlinkCommandHandler unlinkCommandHandler = new UnlinkCommandHandler(this);
        getCommand("unlink").setExecutor(new UnlinkCommand(unlinkCommandHandler));

        getCommand("discord").setExecutor(new DiscordCommand(commandRegistry, reloadService));
        getCommand("discord").setTabCompleter(new DiscordCompleter(commandRegistry));

        registerListeners();
        hookLuckPerms();

        this.logService = new LogService(this);
        logService.logServerStart();

        PluginLogger.info("Plugin has been enabled!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(this), this);
    }

    private void hookLuckPerms() {
        final Plugin luckPermsPlugin = getServer().getPluginManager().getPlugin("LuckPerms");

        if (luckPermsPlugin == null) {
            PluginLogger.info("LuckPerms plugin is missing!");
            return;
        }

        final RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null && provider.getProvider() != null) {
            final LuckPerms luckPerms = provider.getProvider();
            this.lpSyncManager = new LuckPermsSyncManager(this);
            lpSyncManager.init(luckPerms);
        } else {
            PluginLogger.error("LuckPerms provider unavailable.");
        }
    }

    @Override
    public void onDisable() {
        PluginLogger.info("Plugin is shutting down...");
        logService.logServerStop();

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


    public LogService getLogService() {
        return logService;
    }

    public Permission getPermission() {
        return permission;
    }

    public LuckPermsSyncManager getLpSyncManager() {
        return lpSyncManager;
    }
}