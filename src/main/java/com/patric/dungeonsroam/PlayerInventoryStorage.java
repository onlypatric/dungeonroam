package com.patric.dungeonsroam;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Keeps track of snapshot inventories for normal play and RPG worlds per player.
 */
public final class PlayerInventoryStorage {
    private final Map<UUID, InventorySnapshot> normalSnapshots = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, Map<String, InventorySnapshot>> rpgSnapshots = Collections.synchronizedMap(new HashMap<>());

    public void saveNormalSnapshot(UUID playerId, InventorySnapshot snapshot) {
        normalSnapshots.put(playerId, snapshot);
    }

    public Optional<InventorySnapshot> getNormalSnapshot(UUID playerId) {
        return Optional.ofNullable(normalSnapshots.get(playerId));
    }

    public void clearNormalSnapshot(UUID playerId) {
        normalSnapshots.remove(playerId);
    }

    public void saveRpgSnapshot(UUID playerId, String world, InventorySnapshot snapshot) {
        rpgSnapshots
                .computeIfAbsent(playerId, ignored -> new HashMap<>())
                .put(world, snapshot);
    }

    public Optional<InventorySnapshot> getRpgSnapshot(UUID playerId, String world) {
        Map<String, InventorySnapshot> perWorld = rpgSnapshots.get(playerId);
        if (perWorld == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(perWorld.get(world));
    }

    public void removeRpgSnapshot(UUID playerId, String world) {
        Map<String, InventorySnapshot> perWorld = rpgSnapshots.get(playerId);
        if (perWorld == null) {
            return;
        }
        perWorld.remove(world);
        if (perWorld.isEmpty()) {
            rpgSnapshots.remove(playerId);
        }
    }

    @Nullable
    public InventorySnapshot capture(org.bukkit.entity.Player player) {
        ItemStack[] offhand = null;
        ItemStack offItem = player.getInventory().getItemInOffHand();
        if (offItem != null) {
            offhand = new ItemStack[]{offItem.clone()};
        }

        return new InventorySnapshot(
                player.getInventory().getContents(),
                player.getInventory().getArmorContents(),
                offhand,
                player.getEnderChest().getContents()
        );
    }
}
