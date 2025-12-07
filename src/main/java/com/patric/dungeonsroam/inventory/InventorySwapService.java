package com.patric.dungeonsroam.inventory;

import com.patric.dungeonsroam.DungeonRoamPlugin;
import com.patric.dungeonsroam.config.ConfigService;
import com.patric.dungeonsroam.core.RpgWorldService;
import com.patric.dungeonsroam.data.PlayerDataManager;
import org.bukkit.entity.Player;

public class InventorySwapService {

    private final DungeonRoamPlugin plugin;
    private final RpgWorldService rpgWorldService;
    private final PlayerDataManager playerDataManager;

    public InventorySwapService(DungeonRoamPlugin plugin, RpgWorldService rpgWorldService, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.rpgWorldService = rpgWorldService;
        this.playerDataManager = playerDataManager;
    }

    public void handleWorldChange(Player player, String fromWorld, String toWorld) {
        if (fromWorld == null || toWorld == null || fromWorld.equalsIgnoreCase(toWorld)) {
            return;
        }

        boolean fromRpg = rpgWorldService.isRpgWorld(fromWorld);
        boolean toRpg = rpgWorldService.isRpgWorld(toWorld);

        if (!fromRpg && toRpg) {
            enterRpgWorld(player, fromWorld, toWorld);
        } else if (fromRpg && !toRpg) {
            exitRpgWorld(player, fromWorld, toWorld);
        } else if (fromRpg) {
            swapBetweenRpgWorlds(player, fromWorld, toWorld);
        }
    }

    private void enterRpgWorld(Player player, String fromWorld, String targetWorld) {
        debug("Saving original inventory for " + player.getName() + " from " + fromWorld + " before entering " + targetWorld);
        playerDataManager.saveOriginalInventory(player.getUniqueId(), fromWorld, InventorySnapshot.fromPlayer(player));

        InventorySnapshot rpgInventory = playerDataManager.loadRpgInventory(player.getUniqueId(), targetWorld);
        applySnapshotOrClear(player, rpgInventory);
        debug("Applied RPG inventory for " + player.getName() + " in " + targetWorld);
    }

    private void exitRpgWorld(Player player, String fromWorld, String targetWorld) {
        debug("Saving RPG inventory for " + player.getName() + " in " + fromWorld + " before returning to " + targetWorld);
        playerDataManager.saveRpgInventory(player.getUniqueId(), fromWorld, InventorySnapshot.fromPlayer(player));

        InventorySnapshot originalInventory = playerDataManager.loadOriginalInventory(player.getUniqueId(), fromWorld);
        applySnapshotOrClear(player, originalInventory);
        debug("Restored original inventory for " + player.getName() + " after leaving " + fromWorld);
    }

    private void swapBetweenRpgWorlds(Player player, String fromWorld, String targetWorld) {
        debug("Swapping RPG inventories for " + player.getName() + " from " + fromWorld + " to " + targetWorld);
        playerDataManager.saveRpgInventory(player.getUniqueId(), fromWorld, InventorySnapshot.fromPlayer(player));
        InventorySnapshot destinationSnapshot = playerDataManager.loadRpgInventory(player.getUniqueId(), targetWorld);
        applySnapshotOrClear(player, destinationSnapshot);
    }

    public void ensureRpgInventory(Player player) {
        String worldName = player.getWorld().getName();
        if (!rpgWorldService.isRpgWorld(worldName)) {
            return;
        }
        InventorySnapshot snapshot = playerDataManager.loadRpgInventory(player.getUniqueId(), worldName);
        applySnapshotOrClear(player, snapshot);
        debug("Ensured RPG inventory applied for " + player.getName() + " in " + worldName);
    }

    private void applySnapshotOrClear(Player player, InventorySnapshot snapshot) {
        if (snapshot == null) {
            player.getInventory().clear();
            player.getEnderChest().clear();
            return;
        }
        snapshot.apply(player);
    }

    public void saveActiveRpgState(Player player) {
        String worldName = player.getWorld().getName();
        if (!rpgWorldService.isRpgWorld(worldName)) {
            return;
        }
        playerDataManager.saveRpgInventory(player.getUniqueId(), worldName, InventorySnapshot.fromPlayer(player));
    }

    private void debug(String message) {
        ConfigService configService = plugin.services().getConfigService();
        if (configService.getConfig().getLogging().isDebug()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
}
