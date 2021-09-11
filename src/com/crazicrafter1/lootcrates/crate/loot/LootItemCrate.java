package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.crutils.Util;
import org.bukkit.inventory.ItemStack;

public class LootItemCrate extends LootItem {

    private final Crate crate;

    public LootItemCrate(Crate crate, int min, int max) {
        super(null, min, max);
        this.crate = crate;
    }

    @Override
    public ItemStack getIcon() {
        return crate.getItemStack(Util.randomRange(min, max));
    }
}
