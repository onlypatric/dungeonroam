package com.patric.dungeonsroam;

import com.patric.dungeonsroam.command.RpgCommandExecutor;
import com.patric.dungeonsroam.config.ConfigService;
import com.patric.dungeonsroam.core.ServiceRegistry;
import com.patric.dungeonsroam.core.RpgWorldService;
import com.patric.dungeonsroam.data.PlayerDataManager;
import com.patric.dungeonsroam.inventory.InventorySwapService;
import com.patric.dungeonsroam.listener.CombatProtectionListener;
import com.patric.dungeonsroam.listener.JoinQuitListener;
import com.patric.dungeonsroam.listener.WorldTransitionListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;

/**
 * Primary plugin bootstrap for DungeonRoam.
 */
public class DungeonRoamPlugin extends JavaPlugin {

    private ServiceRegistry services;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ConfigService configService = new ConfigService(this);
        configService.reload();

        PlayerDataManager playerDataManager = new PlayerDataManager(this);
        RpgWorldService rpgWorldService = new RpgWorldService(configService);
        InventorySwapService inventorySwapService = new InventorySwapService(this, rpgWorldService, playerDataManager);

        services = new ServiceRegistry(configService, rpgWorldService, playerDataManager, inventorySwapService);

        registerListeners();
        registerCommands();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
                new WorldTransitionListener(services.getInventorySwapService()), this);
        getServer().getPluginManager().registerEvents(
                new JoinQuitListener(services.getRpgWorldService(), services.getInventorySwapService()), this);
        getServer().getPluginManager().registerEvents(
                new CombatProtectionListener(services.getRpgWorldService(), services.getConfigService()), this);
    }

    private void registerCommands() {
        PluginCommand command = getCommand("rpg");
        if (command != null) {
            command.setExecutor(new RpgCommandExecutor(this, services));
        } else {
            getLogger().warning("/rpg command missing from plugin.yml; command executor not registered.");
        }
    }

    public ServiceRegistry services() {
        return services;
    }
}
