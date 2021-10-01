package com.crazicrafter1.lootcrates.DEPRECATED;

import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LootItemEnchantable extends LootItem {

    private final List<QEnchantment> enchantments;

    public LootItemEnchantable(Map<String, Object> args) {
        super(args);
        enchantments = (List<QEnchantment>) args.get("enchantments");
    }

    public LootItemEnchantable(ItemStack itemStack, List<QEnchantment> enchantments) {
        super(itemStack, 1, 1);
        this.enchantments = enchantments;
    }

    @Override
    public ItemStack getIcon() {

        ItemStack itemStack = super.getIcon();

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

    @Override
    public String toString() {
        return null;
    }

    public static class QEnchantment implements ConfigurationSerializable {

        private final Enchantment enchantment;
        private final int min;
        private final int max;

        public QEnchantment(Map<String, Object> args) {
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft((String)args.get("enchantment")));
            min = (int) args.get("min");
            max = (int) args.get("max");
        }

        public QEnchantment(Enchantment enchantment, int count) {
            this(enchantment, count, count);
        }

        public QEnchantment(Enchantment enchantment, int min, int max) {
            this.enchantment = enchantment;
            this.min = min;
            this.max = max;
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new LinkedHashMap<>();

            result.put("enchantment", enchantment.getKey().getKey());
            result.put("min", min);
            result.put("max", max);

            return result;
        }
    }

}
