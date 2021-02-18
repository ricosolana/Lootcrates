package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.crate.Crate;

public class LootCrate extends LootItem {

    public LootCrate(Crate crate, int min, int max) {
        super(crate.getPreppedItemStack(false), min, max);
    }
}
