package com.patric.dungeonsroam;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;


/**
 * Handles swapping between the player's normal and RPG inventories when crossing world boundaries.
 */
public final class InventorySwapService {
    private final JavaPlugin plugin;
    private final RPGConfig config;
    private final PlayerInventoryStorage storage;
    private final PlayerDataManager dataManager;
    private final Logger logger;

    public InventorySwapService(JavaPlugin plugin, RPGConfig config, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.config = config;
        this.storage = new PlayerInventoryStorage();
        this.dataManager = dataManager;
        this.logger = plugin.getLogger();
    }

    public void handleTransition(@NotNull Player player, @NotNull World from, @NotNull World to) {
        boolean wasInRpg = config.isRpgWorld(from.getName());
        boolean nowInRpg = config.isRpgWorld(to.getName());
        if (wasInRpg == nowInRpg) {
            return;
        }

        UUID playerId = player.getUniqueId();
        if (nowInRpg) {
            InventorySnapshot normalSnapshot = storage.capture(player);
            if (normalSnapshot != null) {
                storage.saveNormalSnapshot(playerId, normalSnapshot);
                logger.fine(() -> String.format("Saving normal inventory for %s before entering RPG world %s.",
                        player.getName(), to.getName()));
            }

            getRpgSnapshot(playerId, to.getName()).ifPresent(snapshot -> applySnapshot(player, snapshot));
        } else {
            InventorySnapshot currentSnapshot = storage.capture(player);
            if (currentSnapshot != null) {
                storage.saveRpgSnapshot(playerId, from.getName(), currentSnapshot);
                dataManager.saveRpgSnapshot(playerId, from.getName(), currentSnapshot);
            }
            storage.getNormalSnapshot(playerId)
                    .ifPresent(saved -> applySnapshot(player, saved));
            storage.clearNormalSnapshot(playerId);
        }
    }

    private Optional<InventorySnapshot> getRpgSnapshot(UUID playerId, String world) {
        Optional<InventorySnapshot> memorySnapshot = storage.getRpgSnapshot(playerId, world);
        if (memorySnapshot.isPresent()) {
            return memorySnapshot;
        }
        Optional<InventorySnapshot> persisted = dataManager.loadRpgSnapshot(playerId, world);
        persisted.ifPresent(snapshot -> storage.saveRpgSnapshot(playerId, world, snapshot));
        return persisted;
    }

    private void applySnapshot(Player player, InventorySnapshot snapshot) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = snapshot.getContents();
        if (contents != null) {
            inventory.setContents(contents);
        }

        ItemStack[] armor = snapshot.getArmor();
        if (armor != null) {
            inventory.setArmorContents(armor);
        }

        ItemStack[] offhand = snapshot.getOffHand();
        if (offhand != null && offhand.length > 0) {
            inventory.setItemInOffHand(offhand[0]);
        } else {
            inventory.setItemInOffHand(null);
        }
        player.getEnderChest().setContents(snapshot.getEnderChest());
    }

    @NotNull
    public PlayerInventoryStorage getStorage() {
        return storage;
    }
}
