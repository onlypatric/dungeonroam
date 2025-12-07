package com.patric.dungeonsroam.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventorySnapshot {
    private ItemStack[] contents;
    private ItemStack[] armorContents;
    private ItemStack offHand;
    private ItemStack[] enderChestContents;

    public static InventorySnapshot fromPlayer(Player player) {
        PlayerInventory inventory = player.getInventory();
        InventorySnapshot snapshot = new InventorySnapshot();
        snapshot.contents = inventory.getContents().clone();
        snapshot.armorContents = inventory.getArmorContents().clone();
        snapshot.offHand = inventory.getItemInOffHand() == null ? null : inventory.getItemInOffHand().clone();
        snapshot.enderChestContents = player.getEnderChest().getContents().clone();
        return snapshot;
    }

    public void apply(Player player) {
        PlayerInventory inventory = player.getInventory();
        inventory.setContents(contents == null ? new ItemStack[0] : contents.clone());
        inventory.setArmorContents(armorContents == null ? new ItemStack[0] : armorContents.clone());
        inventory.setItemInOffHand(offHand == null ? null : offHand.clone());
        player.getEnderChest().setContents(enderChestContents == null ? new ItemStack[0] : enderChestContents.clone());
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public ItemStack[] getArmorContents() {
        return armorContents;
    }

    public void setArmorContents(ItemStack[] armorContents) {
        this.armorContents = armorContents;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    public ItemStack[] getEnderChestContents() {
        return enderChestContents;
    }

    public void setEnderChestContents(ItemStack[] enderChestContents) {
        this.enderChestContents = enderChestContents;
    }
}
