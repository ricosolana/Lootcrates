package com.crazicrafter1.lootcrates.crate;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class LootGroup implements ConfigurationSerializable {

    public String name;
    public ItemStack itemStack;
    public ArrayList<AbstractLoot> loot;

    public LootGroup(String id, ItemStack itemStack, ArrayList<AbstractLoot> loot) {
        this.name = id;
        this.itemStack = itemStack;
        this.loot = loot;
    }

    public LootGroup(Map<String, Object> args) {
        itemStack = (ItemStack) args.get("itemStack");
        loot = (ArrayList<AbstractLoot>) args.get("loot");
    }

    public AbstractLoot getRandomLoot() {
        return loot.get((int) (Math.random() * loot.size()));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("itemStack", itemStack);
        result.put("loot", loot);

        return result;
    }

    @Override
    public String toString() {
        return "LootGroup{" +
                "name='" + name + '\'' +
                ", itemStack=" + itemStack.getType() +
                ", loot=" + loot +
                '}';
    }
}
