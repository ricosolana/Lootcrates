package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.util.Util;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class LootEnchantableItem extends LootItem {

    private final QEnchantment[] enchantments;

    public LootEnchantableItem(ItemStack itemStack, QEnchantment[] enchantments) {
        super(itemStack, 1, 1);
        this.enchantments = enchantments;
    }

    @Override
    public ItemStack getAccurateVisual() {

        ItemStack itemStack = super.getBaseVisual();

        if (itemStack.getType() == Material.ENCHANTED_BOOK) {

            EnchantmentStorageMeta meta = (EnchantmentStorageMeta)itemStack.getItemMeta();

            assert meta != null;

            for (QEnchantment e : enchantments) {
                meta.addStoredEnchant(e.enchantment, Util.randomRange(e.min, e.max), true);
            }

            itemStack.setItemMeta(meta);

        } else {
            for (QEnchantment e : enchantments) {
                itemStack.addUnsafeEnchantment(e.enchantment, Util.randomRange(e.min, e.max));
            }

        }

        return itemStack;
    }

    public static class QEnchantment {

        private final Enchantment enchantment;
        private final int min;
        private final int max;

        public QEnchantment(Enchantment enchantment, int count) {
            this(enchantment, count, count);
        }

        public QEnchantment(Enchantment enchantment, int min, int max) {
            this.enchantment = enchantment;
            this.min = min;
            this.max = max;
        }
    }

}
