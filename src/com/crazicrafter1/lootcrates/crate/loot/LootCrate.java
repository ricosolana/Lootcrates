package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.util.Util;
import org.bukkit.inventory.ItemStack;

public class LootCrate extends LootItem {

    private Crate crate;

    public LootCrate(Crate crate, int min, int max) {
        //super(crate.getItemStack(1), min, max);
        super(null, min, max);
        this.crate = crate;
    }

    @Override
    public ItemStack getAccurateVisual() {
        // just return the crate item (since seasonal can change it)
        return crate.getItemStack(Util.randomRange(min, max));
    }
}
