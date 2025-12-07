package com.patric.dungeonsroam.listener;

import com.patric.dungeonsroam.config.ConfigService;
import com.patric.dungeonsroam.core.RpgWorldService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatProtectionListener implements Listener {

    private final RpgWorldService rpgWorldService;
    private final ConfigService configService;

    public CombatProtectionListener(RpgWorldService rpgWorldService, ConfigService configService) {
        this.rpgWorldService = rpgWorldService;
        this.configService = configService;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        if (!rpgWorldService.isRpgWorld(event.getEntity().getWorld())) {
            return;
        }

        boolean allowPvp = configService.getConfig().isAllowPvp();
        boolean enforceNoPvp = configService.getConfig().getVanillaOverrides().isEnforceNoPvp();
        if (!allowPvp || enforceNoPvp) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!rpgWorldService.isRpgWorld(event.getLocation().getWorld())) {
            return;
        }

        if (configService.getConfig().getVanillaOverrides().isDisableVanillaMobSpawns()
                && isNaturalSpawn(event.getSpawnReason())) {
            event.setCancelled(true);
        }
    }

    private boolean isNaturalSpawn(CreatureSpawnEvent.SpawnReason reason) {
        return switch (reason) {
            case NATURAL, CHUNK_GEN, DEFAULT, REINFORCEMENTS, JOCKEY, PATROL, RAID, TRIAL_SPAWNER -> true;
            default -> false;
        };
    }
}
