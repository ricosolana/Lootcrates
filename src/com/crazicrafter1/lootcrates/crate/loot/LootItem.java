package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.crutils.Bool;
import com.crazicrafter1.crutils.Util;
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
    public ItemStack getIcon() {
        ItemStack itemStack = super.getIcon();
        itemStack.setAmount(Util.randomRange(min, max));
        return itemStack;
    }

    @Override
    public void execute(ActiveCrate activeCrate, boolean closed, Bool giveItem) {
        // if inventory was closed, items must be automatically given
        if (closed) {
            Util.giveItemToPlayer(activeCrate.getPlayer(), getIcon());
        } else
            giveItem.value = true;
    }
}
