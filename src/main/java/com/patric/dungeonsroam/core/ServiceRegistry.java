package com.patric.dungeonsroam.core;

import com.patric.dungeonsroam.config.ConfigService;
import com.patric.dungeonsroam.data.PlayerDataManager;
import com.patric.dungeonsroam.inventory.InventorySwapService;

public class ServiceRegistry {
    private final ConfigService configService;
    private final RpgWorldService rpgWorldService;
    private final PlayerDataManager playerDataManager;
    private final InventorySwapService inventorySwapService;

    public ServiceRegistry(ConfigService configService,
                           RpgWorldService rpgWorldService,
                           PlayerDataManager playerDataManager,
                           InventorySwapService inventorySwapService) {
        this.configService = configService;
        this.rpgWorldService = rpgWorldService;
        this.playerDataManager = playerDataManager;
        this.inventorySwapService = inventorySwapService;
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public RpgWorldService getRpgWorldService() {
        return rpgWorldService;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public InventorySwapService getInventorySwapService() {
        return inventorySwapService;
    }
}
