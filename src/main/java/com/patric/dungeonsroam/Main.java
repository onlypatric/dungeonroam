package com.patric.dungeonsroam;

import co.aikar.commands.PaperCommandManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for the DungeonRoam plugin.
 */
public final class Main extends JavaPlugin {
    private static Main instance;

    private RPGConfig rpgConfig;
    private InventorySwapService inventorySwapService;
    private PlayerDataManager playerDataManager;
    private KitManager kitManager;
    private EconomyManager economyManager;
    private ShopManager shopManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.rpgConfig = new RPGConfig(getConfig());
        this.playerDataManager = new PlayerDataManager(this);
        this.kitManager = new KitManager(this);
        this.economyManager = new EconomyManager(this);
        this.shopManager = new ShopManager(this);
        this.inventorySwapService = new InventorySwapService(this, rpgConfig, playerDataManager);

        registerListeners();
        registerCommands();

        getLogger().info("DungeonRoam enabled for " + rpgConfig.getRpgWorlds().size() + " configured RPG worlds.");
    }

    @Override
    public void onDisable() {
        playerDataManager.flush();
        getLogger().info("DungeonRoam disabled.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new WorldTransitionListener(inventorySwapService), this);
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        commandManager.registerCommand(new RpgRootCommand(inventorySwapService, kitManager, economyManager, shopManager));
    }

    public static Main getInstance() {
        return instance;
    }

    public RPGConfig getRpgConfig() {
        return rpgConfig;
    }
}
