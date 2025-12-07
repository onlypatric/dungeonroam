package com.patric.dungeonsroam.config;

import com.patric.dungeonsroam.DungeonRoamPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigService {
    private final DungeonRoamPlugin plugin;
    private final RpgConfig config = new RpgConfig();

    public ConfigService(DungeonRoamPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration yaml = plugin.getConfig();

        config.setRpgWorlds(yaml.getStringList("rpgWorlds"));
        config.setUseWorldGuard(yaml.getBoolean("useWorldGuard", true));
        config.setRegionInventorySwitch(yaml.getBoolean("regionInventorySwitch", false));
        config.setAllowPvp(yaml.getBoolean("allowPVP", false));
        config.setSafeRegions(yaml.getStringList("safeRegions"));

        config.getInventory().setPreferSharedEnderChest(yaml.getBoolean("inventory.preferSharedEnderChest", false));

        config.getEconomy().setUseVault(yaml.getBoolean("economy.useVault", true));
        config.getEconomy().setUseXConomy(yaml.getBoolean("economy.useXConomy", true));
        config.getEconomy().setStartingBalance(yaml.getDouble("economy.startingBalance", 0.0));
        config.getEconomy().setCurrencyName(yaml.getString("economy.currencyName", "Coins"));

        config.getKitBuilder().setEnabled(yaml.getBoolean("kitBuilder.enabled", true));
        config.getKitBuilder().setForceSafeZones(yaml.getBoolean("kitBuilder.forceSafeZones", true));

        config.getShop().setTitle(yaml.getString("shop.title", "&8Open World RPG Shop"));
        config.getShop().setRows(yaml.getInt("shop.rows", 3));

        config.getVanillaOverrides().setDisableVanillaMobSpawns(
                yaml.getBoolean("vanillaOverrides.disableVanillaMobSpawns", false));
        config.getVanillaOverrides().setEnforceNoPvp(
                yaml.getBoolean("vanillaOverrides.enforceNoPvp", !config.isAllowPvp()));

        config.getLogging().setDebug(yaml.getBoolean("logging.debug", false));
    }

    public RpgConfig getConfig() {
        return config;
    }
}
