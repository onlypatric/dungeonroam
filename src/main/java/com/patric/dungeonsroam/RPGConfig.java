package com.patric.dungeonsroam;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

/**
 * Reads configuration values relevant to RPG mode.
 */
public final class RPGConfig {
    private final List<String> rpgWorlds;
    private final boolean useWorldGuard;
    private final boolean regionInventorySwitch;
    private final boolean allowPvp;

    public RPGConfig(FileConfiguration configuration) {
        this.rpgWorlds = configuration.getStringList("rpgWorlds");
        this.useWorldGuard = configuration.getBoolean("useWorldGuard", false);
        this.regionInventorySwitch = configuration.getBoolean("regionInventorySwitch", false);
        this.allowPvp = configuration.getBoolean("allowPVP", true);
    }

    public List<String> getRpgWorlds() {
        return Collections.unmodifiableList(rpgWorlds);
    }

    public boolean isRpgWorld(String worldName) {
        return rpgWorlds.contains(worldName);
    }

    public boolean useWorldGuard() {
        return useWorldGuard;
    }

    public boolean regionInventorySwitch() {
        return regionInventorySwitch;
    }

    public boolean allowPvp() {
        return allowPvp;
    }
}
