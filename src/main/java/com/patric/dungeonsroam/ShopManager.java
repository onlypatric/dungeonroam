package com.patric.dungeonsroam;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Stub for the RPG shop system.
 */
public final class ShopManager {
    private final JavaPlugin plugin;

    public ShopManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void openShop(Player player) {
        player.sendMessage(Component.text("Shop GUI coming soon.").color(NamedTextColor.GOLD));
        plugin.getLogger().fine(() -> player.getName() + " opened the RPG shop.");
    }
}
