package com.patric.dungeonsroam.listener;

import com.patric.dungeonsroam.core.RpgWorldService;
import com.patric.dungeonsroam.inventory.InventorySwapService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    private final RpgWorldService rpgWorldService;
    private final InventorySwapService inventorySwapService;

    public JoinQuitListener(RpgWorldService rpgWorldService, InventorySwapService inventorySwapService) {
        this.rpgWorldService = rpgWorldService;
        this.inventorySwapService = inventorySwapService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        inventorySwapService.ensureRpgInventory(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (rpgWorldService.isRpgWorld(event.getPlayer().getWorld())) {
            inventorySwapService.saveActiveRpgState(event.getPlayer());
        }
    }
}
