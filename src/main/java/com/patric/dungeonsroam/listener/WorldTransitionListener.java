package com.patric.dungeonsroam.listener;

import com.patric.dungeonsroam.inventory.InventorySwapService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldTransitionListener implements Listener {

    private final InventorySwapService inventorySwapService;
    private final Set<UUID> handledTeleport = ConcurrentHashMap.newKeySet();

    public WorldTransitionListener(InventorySwapService inventorySwapService) {
        this.inventorySwapService = inventorySwapService;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled() || event.getTo() == null || event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            return;
        }
        handledTeleport.add(event.getPlayer().getUniqueId());
        inventorySwapService.handleWorldChange(
                event.getPlayer(),
                event.getFrom().getWorld().getName(),
                event.getTo().getWorld().getName());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (handledTeleport.remove(playerId)) {
            return;
        }

        inventorySwapService.handleWorldChange(
                event.getPlayer(),
                event.getFrom().getName(),
                event.getPlayer().getWorld().getName());
    }
}
