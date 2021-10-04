package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;

import java.util.HashMap;
import java.util.Map;

public class LootItemCrate extends LootOrdinateItem {

    //private final String crate;
    private final Crate crate;

    /*
     * Default constructor
     */
    public LootItemCrate() {
        crate = null;
    }

    public LootItemCrate(Map<String, Object> args) {
        super(Main.get().data.crates.get((String)args.get("crate")).itemStack, args);
        crate = Main.get().data.crates.get((String)args.get("crate"));
    }

    public LootItemCrate(Crate crate) {
        super(crate.itemStack, 1, 1);
        this.crate = crate;
    }

    @Override
    public String toString() {
        return "crate: &7" + crate + "\n" +
                super.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>(); // = super.serialize();

        result.put("crate", crate.name);

        return result;
    }
}
