package com.patric.dungeonsroam;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Root /rpg command stub used by ACF. Future subcommands can be added here.
 */
@CommandAlias("rpg|openworldrpg")
@CommandPermission("openworldrpg.use")
public final class RpgRootCommand extends BaseCommand {
    private final InventorySwapService inventorySwapService;
    private final KitManager kitManager;
    private final EconomyManager economyManager;
    private final ShopManager shopManager;

    public RpgRootCommand(InventorySwapService inventorySwapService,
                          KitManager kitManager,
                          EconomyManager economyManager,
                          ShopManager shopManager
    ) {
        this.inventorySwapService = inventorySwapService;
        this.kitManager = kitManager;
        this.economyManager = economyManager;
        this.shopManager = shopManager;
    }

    @Default
    public void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("DungeonRoam commands: /rpg builder, /rpg stats, /rpg shop").color(NamedTextColor.YELLOW));
    }

    @Subcommand("builder")
    @CommandPermission("openworldrpg.builder")
    public void openBuilder(Player player) {
        player.sendMessage(Component.text("Kit builder coming soon. ").color(NamedTextColor.GREEN)
                .append(Component.text("Loaded kits: " + kitManager.getLoadedKitCount()).color(NamedTextColor.GRAY)));
    }

    @Subcommand("stats")
    public void showStats(Player player) {
        if (economyManager.hasEconomy()) {
            double balance = economyManager.getBalance(player.getUniqueId());
            player.sendMessage(Component.text("Balance: " + balance).color(NamedTextColor.AQUA));
        } else {
            player.sendMessage(Component.text("Economy is unavailable; using internal ledger.").color(NamedTextColor.GRAY));
        }
    }

    @Subcommand("shop")
    @CommandPermission("openworldrpg.shop")
    public void openShop(Player player) {
        shopManager.openShop(player);
    }
}
