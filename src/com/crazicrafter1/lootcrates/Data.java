package com.crazicrafter1.lootcrates;

import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Data implements ConfigurationSerializable {

    public Data() {}

    public Data(Map<String, Object> args) {
        debug = (boolean) args.getOrDefault("debug", false);
        update = (boolean) args.getOrDefault("update", true);
        speed = (int) args.getOrDefault("speed", 4);

        unSelectedItem = (ItemStack) args.get("unSelectedItem");
        selectedItem = (ItemStack) args.get("selectedItem");

        // load in the same way, but need to pass name somehow
        lootGroups = (LinkedHashMap<String, LootGroup>) args.get("lootGroups");
        for (Map.Entry<String, LootGroup> entry : lootGroups.entrySet()) {
            entry.getValue().name = entry.getKey();
            Main.getInstance().info("Name: " + entry.getKey());
        }

        //Main.getInstance().info(Data.lootGroups.toString());

        crates = (LinkedHashMap<String, Crate>) args.get("crates");
        for (Map.Entry<String, Crate> entry : crates.entrySet()) {
            String name = entry.getKey();
            Crate crate = entry.getValue();

            crate.name = name;
            crate.itemStack = Crate.makeCrate(crate.itemStack, name);

            //for (Map.Entry<String, Integer> entry1 : crate.lootByName.entrySet()) {
            //    // initialize sums
            //    crate.lootBySum.put(Data.lootGroups.get(entry1.getKey()), entry1.getValue());
            //}
            // initialize weights
            crate.sumsToWeights();
        }


        /* PRINT
         *
         */

        Main.getInstance().info("---\n");
        //StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        //for (int i=24; i < 34; i++) {
        //    Main.getInstance().info(stes[i].toString());
        //}

        for (Map.Entry<String, LootGroup> entry : lootGroups.entrySet()) {
            Main.getInstance().info("Name: " + entry.getValue().name);
        }

        Main.getInstance().info("---\n");

        //Main.getInstance().info("final: ");
        //Main.getInstance().info(Data.lootGroups.toString());

        fireworkEffect = (FireworkEffect) args.get("fireworkEffect");

        totalOpens = (int) args.getOrDefault("totalOpens", 0);
    }

    /*
     * Serializable stuff
     */
    public static boolean debug;
    public static boolean update;
    public static int speed;

    public static ItemStack unSelectedItem;
    public static ItemStack selectedItem;
    public static FireworkEffect fireworkEffect;

    public static HashMap<String, Crate> crates;
    public static HashMap<String, LootGroup> lootGroups;

    public static int totalOpens = 0;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("debug", debug);
        result.put("update", update);
        result.put("speed", speed);

        result.put("unSelectedItem", unSelectedItem);
        result.put("selectedItem", selectedItem);

        result.put("lootGroups", lootGroups);
        result.put("crates", crates);

        result.put("fireworkEffect", fireworkEffect);

        result.put("totalOpens", totalOpens);

        return result;
    }
}
