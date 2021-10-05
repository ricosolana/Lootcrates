package com.crazicrafter1.lootcrates.crate.loot;

import me.zombie_striker.qg.api.QualityArmory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class LootItemQA extends AbstractLootItem {

    public String name;

    /**
     * Default ctor
     */
    public LootItemQA() {}

    public LootItemQA(Map<String, Object> args) {
        super(args);
        this.name = (String)args.get("name");
    }

    public LootItemQA(String name, int min, int max) {
        super(min, max);
        this.name = name;
    }

    @Override
    public ItemStack getIcon() {
        return QualityArmory.getCustomItemAsItemStack(name);
    }

    @Override
    public String toString() {
        return "&8Quality armory: " + name + "\n" +
                super.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("name", name);

        return super.serialize();
    }


}
