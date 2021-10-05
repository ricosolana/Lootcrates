package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class LootItemCrate extends AbstractLootItem {

    // Important to use id, because a referenced Crate object is sort of an
    // small leak
    private String id;

    /**
     * Default ctor
     */
    public LootItemCrate() {}

    public LootItemCrate(Map<String, Object> args) {
        super(args);
        id = (String) args.get("crate");
    }

    public LootItemCrate(Crate crate) {
        this.id = crate.id;
    }

    @Override
    public ItemStack getIcon() {
        return Main.get().data.crates.get(id).itemStack;
    }

    @Override
    public String toString() {
        return "crate: &7" + id + "\n" +
                super.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize(); // = super.serialize();

        result.put("crate", id);

        return result;
    }
}
