package com.patric.dungeonsroam;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Placeholder manager that will ultimately persist player kit selections.
 */
public final class KitManager {
    private final JavaPlugin plugin;
    private final Map<UUID, ItemStack[]> kits = new ConcurrentHashMap<>();

    public KitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("KitManager initialized (" + plugin.getName() + ").");
    }

    public Optional<ItemStack[]> getKit(UUID playerId) {
        return Optional.ofNullable(kits.get(playerId));
    }

    public void registerKit(UUID playerId, ItemStack[] contents) {
        kits.put(playerId, contents);
    }

    public int getLoadedKitCount() {
        return kits.size();
    }
}
