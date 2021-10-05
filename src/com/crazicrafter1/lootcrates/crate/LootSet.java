package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.crate.loot.AbstractLoot;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class LootSet implements ConfigurationSerializable {

    public String id;
    public ItemStack itemStack;
    public ArrayList<AbstractLoot> loot;

    public LootSet(String id, ItemStack itemStack, ArrayList<AbstractLoot> loot) {
        this.id = id;
        this.itemStack = itemStack;
        this.loot = loot;
    }

    public LootSet(Map<String, Object> args) {
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
                "id='" + id + '\'' +
                ", itemStack=" + itemStack.getType() +
                ", loot=" + loot +
                '}';
    }
}
