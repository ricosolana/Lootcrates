package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.util.Util;
import com.crazicrafter1.lootcrates.crate.AbstractLoot;
import org.bukkit.inventory.ItemStack;

public class LootItem extends AbstractLoot {
    protected final int min;
    protected final int max;

    public LootItem(ItemStack baseItem, int min, int max) {
        super(baseItem);
        this.min = min;
        this.max = max;
    }

    @Override
    public ItemStack getAccurateVisual() {
        ItemStack itemStack = this.getBaseVisual();
        itemStack.setAmount(Util.randomRange(min, max));
        return itemStack;
    }


}
