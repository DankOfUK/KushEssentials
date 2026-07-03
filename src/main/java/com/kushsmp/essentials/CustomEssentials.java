package com.kushsmp.essentials;

import com.kushsmp.essentials.commands.admin.EssCommand;
import com.kushsmp.essentials.commands.kit.GiveKitCommand;
import com.kushsmp.essentials.commands.kit.KitCommand;
import com.kushsmp.essentials.commands.player.AfkCommand;
import com.kushsmp.essentials.commands.player.ClearInventoryCommand;
import com.kushsmp.essentials.commands.player.ContainerCommand;
import com.kushsmp.essentials.commands.player.FlyCommand;
import com.kushsmp.essentials.commands.player.GamemodeCommand;
import com.kushsmp.essentials.commands.player.HatCommand;
import com.kushsmp.essentials.commands.player.HealFeedCommand;
import com.kushsmp.essentials.commands.player.MessageCommand;
import com.kushsmp.essentials.commands.player.MoreCommand;
import com.kushsmp.essentials.commands.player.NearCommand;
import com.kushsmp.essentials.commands.player.RepairCommand;
import com.kushsmp.essentials.commands.staff.BroadcastCommand;
import com.kushsmp.essentials.commands.staff.FreezeCommand;
import com.kushsmp.essentials.commands.staff.GodCommand;
import com.kushsmp.essentials.commands.staff.InvseeCommand;
import com.kushsmp.essentials.commands.staff.ModerationCommand;
import com.kushsmp.essentials.commands.staff.StaffChatCommand;
import com.kushsmp.essentials.commands.staff.VanishCommand;
import com.kushsmp.essentials.commands.teleport.BackCommand;
import com.kushsmp.essentials.commands.teleport.HomeCommand;
import com.kushsmp.essentials.commands.teleport.SpawnCommand;
import com.kushsmp.essentials.commands.teleport.TpCommand;
import com.kushsmp.essentials.commands.teleport.TpaCommand;
import com.kushsmp.essentials.commands.teleport.WarpCommand;
import com.kushsmp.essentials.config.ConfigManager;
import com.kushsmp.essentials.listeners.ChatListener;
import com.kushsmp.essentials.listeners.PlayerListener;
import com.kushsmp.essentials.managers.HomeManager;
import com.kushsmp.essentials.managers.KitManager;
import com.kushsmp.essentials.managers.MuteManager;
import com.kushsmp.essentials.managers.PlayerStateManager;
import com.kushsmp.essentials.managers.TeleportManager;
import com.kushsmp.essentials.managers.WarpManager;
import com.kushsmp.essentials.managers.EconomyManager;
import com.kushsmp.essentials.commands.economy.BalanceCommand;
import com.kushsmp.essentials.commands.economy.PayCommand;
import com.kushsmp.essentials.commands.economy.EcoCommand;
import com.kushsmp.essentials.commands.economy.BalanceTopCommand;
import com.kushsmp.essentials.managers.RankManager;
import com.kushsmp.essentials.managers.PermissionManager;
import com.kushsmp.essentials.managers.TabManager;
import com.kushsmp.essentials.util.PapiBridge;
import com.kushsmp.essentials.commands.admin.RankCommand;
import com.kushsmp.essentials.listeners.ConnectionListener;
import com.kushsmp.essentials.listeners.ServerListListener;
import com.kushsmp.essentials.placeholder.KushExpansion;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * CustomEssentials entry point. Boots the config layer, all feature managers,
 * registers commands and listeners, and starts the AFK/freeze tick task.
 */
public class CustomEssentials extends JavaPlugin {

    private ConfigManager configManager;

    private HomeManager homeManager;
    private WarpManager warpManager;
    private TeleportManager teleportManager;
    private KitManager kitManager;
    private PlayerStateManager stateManager;
    private MuteManager muteManager;

    private PapiBridge papiBridge;
    private RankManager rankManager;
    private PermissionManager permissionManager;
    private TabManager tabManager;
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        // --- Config & data ---
        configManager = new ConfigManager(this);
        configManager.loadAll();

        // --- Managers ---
        homeManager = new HomeManager(this);
        warpManager = new WarpManager(this);
        teleportManager = new TeleportManager(this);
        kitManager = new KitManager(this);
        stateManager = new PlayerStateManager(this);
        muteManager = new MuteManager(this);

        // --- Ranks / permissions / tab / placeholders ---
        papiBridge = new PapiBridge(this);
        rankManager = new RankManager(this);
        rankManager.load();
        permissionManager = new PermissionManager(this);
        tabManager = new TabManager(this);

        // --- Economy ---
        economyManager = new EconomyManager(this);
        registerVaultEconomy();

        if (papiBridge.isEnabled()) {
            try {
                new KushExpansion(this).register();
            } catch (Throwable t) {
                getLogger().warning("Could not register PlaceholderAPI expansion: " + t.getMessage());
            }
        }

        // --- Commands ---
        registerCommands();

        // --- Listeners ---
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerListListener(this), this);
        getServer().getPluginManager().registerEvents(new com.kushsmp.essentials.gui.KitMenuListener(this), this);

        // Catch up anyone already online (e.g. after a /reload).
        for (Player online : getServer().getOnlinePlayers()) {
            permissionManager.apply(online);
        }
        tabManager.start();

        // --- Background tasks (auto-AFK + frozen leash check) ---
        startTickTask();

        getLogger().info("CustomEssentials enabled.");
    }

    @Override
    public void onDisable() {
        if (tabManager != null) tabManager.stop();
        if (permissionManager != null) permissionManager.removeAll();
        getLogger().info("CustomEssentials disabled.");
    }

    private void registerCommands() {
        // Teleport / homes / warps
        HomeCommand home = new HomeCommand(this);
        bind(home, "sethome", "home", "delhome", "homes");

        WarpCommand warp = new WarpCommand(this);
        bind(warp, "setwarp", "warp", "delwarp", "warps");

        SpawnCommand spawn = new SpawnCommand(this);
        bind(spawn, "spawn", "setspawn");

        TpaCommand tpa = new TpaCommand(this);
        bind(tpa, "tpa", "tpahere", "tpaccept", "tpdeny");

        TpCommand tp = new TpCommand(this);
        bind(tp, "tp", "tphere");

        bind(new BackCommand(this), "back");

        // Player utilities
        HealFeedCommand healFeed = new HealFeedCommand(this);
        bind(healFeed, "heal", "feed");

        bind(new FlyCommand(this), "fly");

        GamemodeCommand gm = new GamemodeCommand(this);
        bind(gm, "gamemode", "gmc", "gms", "gma", "gmsp");

        ContainerCommand container = new ContainerCommand(this);
        bind(container, "enderchest", "workbench", "anvil");

        bind(new RepairCommand(this), "repair");
        bind(new ClearInventoryCommand(this), "clearinventory");
        bind(new HatCommand(this), "hat");
        bind(new NearCommand(this), "near");
        bind(new AfkCommand(this), "afk");
        bind(new MoreCommand(this), "more");

        MessageCommand message = new MessageCommand(this);
        bind(message, "msg", "reply");

        // Kits
        KitCommand kit = new KitCommand(this);
        bind(kit, "kit");
        bind(new GiveKitCommand(this), "givekit");

        // Staff
        bind(new VanishCommand(this), "vanish");
        bind(new GodCommand(this), "god");
        bind(new FreezeCommand(this), "freeze");
        bind(new StaffChatCommand(this), "staffchat");
        bind(new BroadcastCommand(this), "broadcast");
        bind(new InvseeCommand(this), "invsee");

        ModerationCommand moderation = new ModerationCommand(this);
        bind(moderation, "kick", "ban", "tempban", "unban", "mute", "unmute");

        // Admin
        bind(new EssCommand(this), "ess");
        bind(new RankCommand(this), "rank");

        // Economy
        bind(new BalanceCommand(this), "balance");
        bind(new PayCommand(this), "pay");
        bind(new BalanceTopCommand(this), "baltop");
        bind(new EcoCommand(this), "eco");
    }

    /** Register our economy as a Vault provider, but only if Vault is installed. */
    private void registerVaultEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        try {
            getServer().getServicesManager().register(
                    net.milkbowl.vault.economy.Economy.class,
                    new com.kushsmp.essentials.vault.VaultEconomyProvider(this),
                    this,
                    org.bukkit.plugin.ServicePriority.Normal);
            getLogger().info("Hooked into Vault as the economy provider.");
        } catch (Throwable t) {
            getLogger().warning("Failed to register Vault economy: " + t.getMessage());
        }
    }

    /** Bind a single executor (also used as tab completer) to one or more command names. */
    private void bind(CommandExecutor executor, String... names) {
        for (String name : names) {
            PluginCommand command = getCommand(name);
            if (command == null) {
                getLogger().warning("Command '" + name + "' is missing from plugin.yml!");
                continue;
            }
            command.setExecutor(executor);
            if (executor instanceof TabCompleter) {
                command.setTabCompleter((TabCompleter) executor);
            }
        }
    }

    private void startTickTask() {
        // Runs every second (20 ticks).
        getServer().getScheduler().runTaskTimer(this, () -> {
            long autoAfk = configManager.getConfig().getLong("regular.afk.auto-afk-seconds", 300) * 1000L;
            boolean afkEnabled = configManager.getConfig().getBoolean("regular.afk.enabled", true);
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                // Auto-AFK
                if (afkEnabled && autoAfk > 0 && !stateManager.isAfk(player.getUniqueId())) {
                    long idle = System.currentTimeMillis() - stateManager.getLastActivity(player.getUniqueId());
                    if (idle >= autoAfk) {
                        stateManager.setAfk(player.getUniqueId(), true);
                        if (configManager.getConfig().getBoolean("regular.afk.broadcast", true)) {
                            getServer().broadcastMessage(cfg().msg("afk.now-afk", "player", player.getName()));
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    // --- Accessors ---
    public ConfigManager config() { return configManager; }
    public HomeManager homes() { return homeManager; }
    public WarpManager warps() { return warpManager; }
    public TeleportManager teleports() { return teleportManager; }
    public KitManager kits() { return kitManager; }
    public PlayerStateManager state() { return stateManager; }
    public MuteManager mutes() { return muteManager; }

    public RankManager ranks() { return rankManager; }
    public PermissionManager permissions() { return permissionManager; }
    public TabManager tab() { return tabManager; }
    public PapiBridge papi() { return papiBridge; }
    public EconomyManager economy() { return economyManager; }

    private ConfigManager cfg() { return configManager; }
}
