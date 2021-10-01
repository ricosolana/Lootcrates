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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LootItemCrate extends LootOrdinateItem {

    //private final String crate;
    private final Crate crate;

    public LootItemCrate(Map<String, Object> args) {
        super(Main.DAT.crates.get((String)args.get("crate")).itemStack, args);
        crate = Main.DAT.crates.get((String)args.get("crate"));
    }

    public LootItemCrate(Crate crate) {
        super(crate.itemStack, 1, 1);
        this.crate = crate;
    }

    @Override
    public String toString() {
        return "crate: " + crate;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>(); // = super.serialize();

        result.put("crate", crate.name);

        return result;
    }
}
