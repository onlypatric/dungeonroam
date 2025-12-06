package com.patric.dungeonsroam;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

/**
 * Stores copies of a player's inventory, armor, offhand and ender-chest contents.
 */
public final class InventorySnapshot {
    private final ItemStack[] contents;
    private final ItemStack[] armor;
    private final ItemStack[] offHand;
    private final ItemStack[] enderChest;

    public InventorySnapshot(ItemStack[] contents, ItemStack[] armor, ItemStack[] offHand, ItemStack[] enderChest) {
        this.contents = copy(contents);
        this.armor = copy(armor);
        this.offHand = copy(offHand);
        this.enderChest = copy(enderChest);
    }

    public ItemStack[] getContents() {
        return copy(contents);
    }

    public ItemStack[] getArmor() {
        return copy(armor);
    }

    public ItemStack[] getOffHand() {
        return copy(offHand);
    }

    public ItemStack[] getEnderChest() {
        return copy(enderChest);
    }

    private static ItemStack[] copy(ItemStack[] source) {
        return source == null ? null : Arrays.stream(source)
                .map(item -> item == null ? null : item.clone())
                .toArray(ItemStack[]::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventorySnapshot)) return false;
        InventorySnapshot that = (InventorySnapshot) o;
        return Arrays.equals(contents, that.contents)
                && Arrays.equals(armor, that.armor)
                && Arrays.equals(offHand, that.offHand)
                && Arrays.equals(enderChest, that.enderChest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(contents), Arrays.hashCode(armor), Arrays.hashCode(offHand), Arrays.hashCode(enderChest));
    }
}
