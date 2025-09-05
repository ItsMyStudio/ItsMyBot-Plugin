package studio.itsmy.itsmybot;

import studio.itsmy.itsmybot.command.DiscordCommand;
import studio.itsmy.itsmybot.command.CommandRegistry;
import studio.itsmy.itsmybot.command.DiscordCompleter;
import studio.itsmy.itsmybot.command.handler.LinkCommand;
import studio.itsmy.itsmybot.command.handler.UnlinkCommand;
import studio.itsmy.itsmybot.command.handler.player.ClaimCommandHandler;
import studio.itsmy.itsmybot.command.handler.player.HelpCommandHandler;
import studio.itsmy.itsmybot.command.handler.player.LinkCommandHandler;
import studio.itsmy.itsmybot.command.handler.player.UnlinkCommandHandler;
import studio.itsmy.itsmybot.file.FilesManager;
import studio.itsmy.itsmybot.listener.PlayerCommandListener;
import studio.itsmy.itsmybot.listener.PlayerJoinListener;
import studio.itsmy.itsmybot.listener.PlayerQuitListener;
import studio.itsmy.itsmybot.service.LogService;
import studio.itsmy.itsmybot.service.ReloadService;
import studio.itsmy.itsmybot.util.PluginLogger;
import studio.itsmy.itsmybot.util.TextFormatter;
import studio.itsmy.itsmybot.ws.handler.role.LuckPermsSyncManager;
import studio.itsmy.itsmybot.ws.WSClient;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main entry point of the ItsMyBot plugin.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Bootstraps text formatting (Adventure / MiniMessage) via {@link TextFormatter}.</li>
 *   <li>Loads configuration and messages files via {@link FilesManager}.</li>
 *   <li>Executes a full reload of runtime configuration via {@link ReloadService} (config + WS client).</li>
 *   <li>Initializes permissions integration (Vault) and optional LuckPerms synchronization.</li>
 *   <li>Registers commands, tab completers, and event listeners.</li>
 *   <li>Sets up WebSocket logging via {@link LogService} (start/stop events, player logs).</li>
 * </ul>
 *
 * <p>Dependencies:
 * <ul>
 *   <li><b>Required:</b> none strictly required to start, but WebSocket features need a valid config.</li>
 *   <li><b>Optional:</b> Vault (group sync), LuckPerms (event bus for role sync), PlaceholderAPI (placeholders), Adventure (messages).</li>
 * </ul>
 */
public class ItsMyBotPlugin extends JavaPlugin {

    /** Centralized logging service (WS-based). */
    private LogService logService;

    /** Vault Permission provider (used for group sync). */
    private Permission permission;

    /** WebSocket client (lazy-initialized). */
    private WSClient wsClient;

    /** LuckPerms mutation tracking manager (optional). */
    private LuckPermsSyncManager lpSyncManager;

    /**
     * Plugin enable hook.
     * <p>
     * Sequence:
     * <ol>
     *   <li>Initialize Adventure audiences and {@link TextFormatter}.</li>
     *   <li>Load config and messages files via {@link FilesManager}.</li>
     *   <li>Reload runtime config and WS client via {@link ReloadService}.</li>
     *   <li>Setup Vault permissions (if present).</li>
     *   <li>Register commands and tab completers.</li>
     *   <li>Register player listeners (join/quit/command).</li>
     *   <li>Hook LuckPerms and subscribe to node mutations (if available).</li>
     *   <li>Start {@link LogService} and log server start.</li>
     * </ol>
     */
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

        // Command registry and commands wiring
        final CommandRegistry commandRegistry = new CommandRegistry();
        commandRegistry.registerCommand(new ClaimCommandHandler(this));
        commandRegistry.registerCommand(new HelpCommandHandler());

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

    /**
     * Registers Bukkit event listeners:
     * <ul>
     *   <li>{@link PlayerJoinListener} – role full sync + join log</li>
     *   <li>{@link PlayerQuitListener} – leave log</li>
     *   <li>{@link PlayerCommandListener} – command log</li>
     * </ul>
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(this), this);
    }

    /**
     * Hooks LuckPerms (if installed) and initializes {@link LuckPermsSyncManager}.
     * <p>
     * Subscribes to {@code NodeMutateEvent} to differentiate expected vs untracked role mutations.
     * Logs a warning if the provider is unavailable.
     */
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

    /**
     * Plugin disable hook.
     * <p>
     * Sends a server stop log, then disconnects and shuts down the WebSocket client
     * to release threads and network resources.
     */
    @Override
    public void onDisable() {
        PluginLogger.info("Plugin is shutting down...");
        logService.logServerStop();

        if (wsClient != null) {
            wsClient.disconnect();
            wsClient.shutdown();
        }
    }

    /**
     * Reloads (reconnects) the WebSocket client according to current configuration.
     * <p>
     * - Lazily creates the client if needed.<br>
     * - Disconnects any existing connection.<br>
     * - Schedules a reconnect after 40 ticks (~2 seconds) on the main thread.
     */
    public void reloadWSClient() {
        if (wsClient == null) {
            wsClient = new WSClient(this);
        }

        wsClient.disconnect();
        getServer().getScheduler().runTaskLater(this, () -> wsClient.connect(), 40L);
    }

    /**
     * Returns the (lazy) WebSocket client instance.
     * <p>
     * Creates a new {@link WSClient} if none exists yet.
     *
     * @return a non-null client instance
     */
    public WSClient getWSClient() {
        if (wsClient == null) {
            wsClient = new WSClient(this);
        }
        return wsClient;
    }

    /**
     * Initializes Vault Permission provider if Vault is installed.
     * <p>
     * Required for group operations in role synchronization.
     * Logs an error if the provider cannot be found.
     */
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

    /**
     * Gets the centralized logging service.
     *
     * @return the {@link LogService} instance
     */
    public LogService getLogService() {
        return logService;
    }

    /**
     * Gets the Vault {@link Permission} provider, if available.
     *
     * @return the permission provider or {@code null} if Vault is missing/unavailable
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Gets the LuckPerms synchronization manager, if available.
     *
     * @return the sync manager or {@code null} if LuckPerms is not hooked
     */
    public LuckPermsSyncManager getLpSyncManager() {
        return lpSyncManager;
    }
}