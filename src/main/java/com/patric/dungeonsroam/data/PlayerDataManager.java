package com.patric.dungeonsroam.data;

import com.patric.dungeonsroam.DungeonRoamPlugin;
import com.patric.dungeonsroam.inventory.InventorySnapshot;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private final DungeonRoamPlugin plugin;
    private final Map<UUID, YamlConfiguration> cache = new ConcurrentHashMap<>();

    public PlayerDataManager(DungeonRoamPlugin plugin) {
        this.plugin = plugin;
    }

    public void saveOriginalInventory(UUID playerId, String worldName, InventorySnapshot snapshot) {
        YamlConfiguration yaml = loadYaml(playerId);
        writeSnapshot(yaml, "original." + worldName, snapshot);
        flushAsync(playerId, yaml);
    }

    public void saveRpgInventory(UUID playerId, String worldName, InventorySnapshot snapshot) {
        YamlConfiguration yaml = loadYaml(playerId);
        writeSnapshot(yaml, "rpg." + worldName, snapshot);
        flushAsync(playerId, yaml);
    }

    public InventorySnapshot loadOriginalInventory(UUID playerId, String worldName) {
        YamlConfiguration yaml = loadYaml(playerId);
        return readSnapshot(yaml, "original." + worldName);
    }

    public InventorySnapshot loadRpgInventory(UUID playerId, String worldName) {
        YamlConfiguration yaml = loadYaml(playerId);
        return readSnapshot(yaml, "rpg." + worldName);
    }

    private void writeSnapshot(YamlConfiguration yaml, String path, InventorySnapshot snapshot) {
        yaml.set(path + ".contents", snapshot.getContents());
        yaml.set(path + ".armor", snapshot.getArmorContents());
        yaml.set(path + ".offHand", snapshot.getOffHand());
        yaml.set(path + ".enderChest", snapshot.getEnderChestContents());
    }

    private InventorySnapshot readSnapshot(YamlConfiguration yaml, String path) {
        if (!yaml.contains(path)) {
            return null;
        }
        InventorySnapshot snapshot = new InventorySnapshot();
        snapshot.setContents(yaml.getList(path + ".contents", Collections.emptyList())
                .toArray(new org.bukkit.inventory.ItemStack[0]));
        snapshot.setArmorContents(yaml.getList(path + ".armor", Collections.emptyList())
                .toArray(new org.bukkit.inventory.ItemStack[0]));
        snapshot.setOffHand(yaml.getItemStack(path + ".offHand"));
        snapshot.setEnderChestContents(yaml.getList(path + ".enderChest", Collections.emptyList())
                .toArray(new org.bukkit.inventory.ItemStack[0]));
        return snapshot;
    }

    private YamlConfiguration loadYaml(UUID playerId) {
        return cache.computeIfAbsent(playerId, this::readFromDisk);
    }

    private YamlConfiguration readFromDisk(UUID playerId) {
        File file = getFile(playerId);
        YamlConfiguration yaml = new YamlConfiguration();
        if (file.exists()) {
            try {
                yaml.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                plugin.getLogger().warning("Failed to load data for " + playerId + ": " + e.getMessage());
            }
        }
        return yaml;
    }

    private void flushAsync(UUID playerId, YamlConfiguration yaml) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> saveNow(playerId, yaml));
    }

    private void saveNow(UUID playerId, YamlConfiguration yaml) {
        try {
            File file = getFile(playerId);
            file.getParentFile().mkdirs();
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save data for " + playerId + ": " + e.getMessage());
        }
    }

    private File getFile(UUID playerId) {
        return new File(new File(plugin.getDataFolder(), "playerdata"), playerId.toString() + ".yml");
    }
}
