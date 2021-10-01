package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.*;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Data implements ConfigurationSerializable {

    public Data(Map<String, Object> args) {
        debug = (boolean) args.get("debug");
        update = (boolean) args.get("update");
        speed = (int) args.get("speed");

        unSelectedItem = (ItemStack) args.get("unSelectedItem");
        selectedItem = (ItemStack) args.get("selectedItem");

        // load in the same way, but need to pass name somehow
        lootGroups = (LinkedHashMap<String, LootGroup>) args.get("lootGroups");
        for (Map.Entry<String, LootGroup> entry : lootGroups.entrySet()) {
            entry.getValue().name = entry.getKey();
        }
        crates = (LinkedHashMap<String, Crate>) args.get("crates");
        //System.out.println(crates);
        System.out.println(args);
        for (Map.Entry<String, Crate> entry : crates.entrySet()) {
            String name = entry.getKey();
            Crate crate = entry.getValue();


            //ItemStack itemStack = Crate.makeCrate((ItemStack) args.get("itemStack"), entry.getKey());

            crate.name = name;
            crate.itemStack = Crate.makeCrate(crate.itemStack, name);

            //Main.getInstance().info("itemStack: " + args.get("itemStack"));
        }

        fireworkEffect = (FireworkEffect) args.get("fireworkEffect");
    }

    /*
     * Serializable stuff
     */
    public boolean debug;// = false;
    public boolean update;// = true;
    public int speed;// = 4;

    public ItemStack unSelectedItem;// = new ItemBuilder(Material.CHEST).name("&f&l???").lore("");
    public ItemStack selectedItem;
    public FireworkEffect fireworkEffect;

    public HashMap<String, Crate> crates;
    public HashMap<String, LootGroup> lootGroups;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap();

        result.put("debug", debug);
        result.put("update", update);
        result.put("speed", speed);

        result.put("unSelectedItem", unSelectedItem);
        result.put("selectedItem", selectedItem);

        result.put("lootGroups", lootGroups);
        result.put("crates", crates);

        result.put("fireworkEffect", fireworkEffect);

        return result;
    }
}
