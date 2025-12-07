package com.patric.dungeonsroam.command;

import com.patric.dungeonsroam.DungeonRoamPlugin;
import com.patric.dungeonsroam.config.ConfigService;
import com.patric.dungeonsroam.core.ServiceRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RpgCommandExecutor implements CommandExecutor {

    private final DungeonRoamPlugin plugin;
    private final ServiceRegistry services;

    public RpgCommandExecutor(DungeonRoamPlugin plugin, ServiceRegistry services) {
        this.plugin = plugin;
        this.services = services;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload":
                if (!sender.hasPermission("openworldrpg.admin.*")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to reload DungeonRoam.");
                    return true;
                }
                services.getConfigService().reload();
                services.getRpgWorldService().refresh();
                sender.sendMessage(ChatColor.GREEN + "DungeonRoam configuration reloaded.");
                return true;
            case "builder":
            case "shop":
            case "stats":
                sender.sendMessage(ChatColor.YELLOW + "This feature is not yet implemented but is reserved by the core plugin.");
                return true;
            case "where":
                if (sender instanceof Player player) {
                    boolean rpg = services.getRpgWorldService().isRpgWorld(player.getWorld());
                    sender.sendMessage(ChatColor.GRAY + "You are currently in " + (rpg ? "an RPG world" : "a vanilla world") + ".");
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                }
                return true;
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        ConfigService config = services.getConfigService();
        sender.sendMessage(ChatColor.AQUA + "DungeonRoam RPG Core");
        sender.sendMessage(ChatColor.GRAY + "RPG Worlds: " + String.join(", ", config.getConfig().getRpgWorlds()));
        sender.sendMessage(ChatColor.GRAY + "WorldGuard Enabled: " + config.getConfig().isUseWorldGuard());
        sender.sendMessage(ChatColor.GRAY + "Region Inventory Switch: " + config.getConfig().isRegionInventorySwitch());
        sender.sendMessage(ChatColor.GRAY + "Allow PvP: " + config.getConfig().isAllowPvp());
        sender.sendMessage(ChatColor.GRAY + "/rpg reload - Reload configuration");
        sender.sendMessage(ChatColor.GRAY + "/rpg builder|shop|stats - Reserved for upcoming features");
    }
}
