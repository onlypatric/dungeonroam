package com.patric.dungeonsroam;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Listens for world changes and delegates to the inventory swap logic.
 */
public final class WorldTransitionListener implements Listener {
    private final InventorySwapService swapService;

    public WorldTransitionListener(InventorySwapService swapService) {
        this.swapService = swapService;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        swapService.handleTransition(event.getPlayer(), event.getFrom(), event.getPlayer().getWorld());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            return;
        }
        swapService.handleTransition(event.getPlayer(), event.getFrom().getWorld(), event.getTo().getWorld());
    }
}
