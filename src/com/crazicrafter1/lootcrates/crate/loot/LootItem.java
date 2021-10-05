package com.crazicrafter1.lootcrates.crate.loot;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class LootItem extends AbstractLootItem {

    public ItemStack itemStack;

    /**
     * Default ctor
     */
    public LootItem() {}

    public LootItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public LootItem(Map<String, Object> args) {
        super(args);
        this.itemStack = (ItemStack) args.get("itemStack");
    }

    @Override
    public ItemStack getIcon() {
        return super.ofRange(itemStack);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("item", itemStack);
        return result;
    }
}
