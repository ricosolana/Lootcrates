package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;

public class LootItemCrate extends LootItem {

    //private final String crate;
    private final Crate crate;

    public LootItemCrate(Crate crate) {
        super(null, 1, 1);
        this.crate = crate;
    }

    @Override
    public ItemStack getIcon() {
        return crate.itemStack;
    }

    @Override
    public String toString() {
        return "crate: " + crate;
    }

    }
}
