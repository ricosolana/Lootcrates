package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.crutils.Util;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.*;

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
        //name = (String) args.get("name");
        itemStack = (ItemStack) args.get("itemStack");
        loot = (ArrayList<AbstractLoot>) args.get("loot");

        Main.getInstance().info(loot.toString());
    }

    public AbstractLoot getRandomLoot() {
        return loot.get((int) (Math.random() * loot.size()));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap();

        //result.put("name", name);
        result.put("itemStack", itemStack);
        result.put("loot", loot);

        return result;
    }

    @Override
    public String toString() {
        return "LootGroup{" +
                "name='" + name + '\'' +
                ", itemStack=" + itemStack.getType() +
                ", loot=" + loot.size() +
                '}';
    }
}
