package com.patric.dungeonsroam;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles hooking to Vault economy if available.
 */
public final class EconomyManager {
    private final JavaPlugin plugin;
    private final Economy economy;

    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.economy = findEconomy();
    }

    private Economy findEconomy() {
        RegisteredServiceProvider<Economy> provider =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            plugin.getLogger().info("Vault economy hooked (" + provider.getProvider().getName() + ").");
            return provider.getProvider();
        }
        plugin.getLogger().warning("Vault economy provider not found; defaulting to internal money ledger.");
        return null;
    }

    public Optional<Economy> getEconomy() {
        return Optional.ofNullable(economy);
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public double getBalance(UUID playerId) {
        if (economy == null) {
            return 0D;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        return economy.getBalance(player);
    }
}
