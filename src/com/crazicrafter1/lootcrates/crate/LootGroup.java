package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.ItemBuilder;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.Result;
import com.crazicrafter1.lootcrates.crate.loot.LootCommand;
import com.crazicrafter1.lootcrates.crate.loot.LootCrate;
import me.zombie_striker.customitemmanager.CustomBaseObject;
import me.zombie_striker.qg.api.QualityArmory;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LootGroup {

    private final String name;
    private final ItemStack itemStack;
    //private ArrayList<String> items; // replace this
    private final AbstractLoot[] loot; // array of multiple loots

    public LootGroup(String name, ItemStack itemStack, AbstractLoot[] abstractLoot) {
        this.name = name;
        this.itemStack = itemStack;
        //this.items = new ArrayList<>();
        //loot = new AbstractLoot[0];
        loot = abstractLoot;
    }

    public String getName() {
        return name;
    }

    public ItemStack getPanel() {
        return itemStack;
    }

    //public ArrayList<String> getParsedItems() {
    //    return items;
    //}

    public AbstractLoot getRandomLoot() {
        return loot[(int) (Math.random() * loot.length)];
    }

    public static LootGroup fromNewConfig(String id) {

        FileConfiguration config = Main.getInstance().getConfig();

        ItemStack item;
        String tempPath;

        {
            tempPath = "gui.lootgroup." + id + ".";
            String itemName = config.getString(tempPath + ".icon");
            String name = config.getString(tempPath + ".title");
            List<String> lore = config.getStringList(tempPath + ".footer");

            //this.name = id;
            item = ItemBuilder.builder(Material.valueOf(itemName)).name(name).lore(lore).toItem();

            //g = new LootGroup()
        }



        /*
            GENERICLOOT LOADING
         */
        ArrayList<AbstractLoot> abstractLoots = new ArrayList<>();

        tempPath = "loot." + id;
        if (!config.contains(tempPath)) {
            Main.getInstance().error("Couldnt find definition for lootgroup '" + id +
                    "' loot in config.");
            return null;
        }

        List<Map<?, ?>> maplist = config.getMapList(tempPath);
        int i = 0;
        for (Map<?, ?> map : maplist) {

            Result result = new Result(null);
            try {

                AbstractLoot abstractLoot = AbstractLoot.fromNewConfig(((Map<String, Object>)map), result);

                if (abstractLoot == null) {
                    Main.getInstance().error("Lootgroup: " + id + "@index: " + i + " (" + result.code.name() + ")");
                    continue;
                }

                abstractLoots.add(abstractLoot);

            } catch (Exception e) {

                Main.getInstance().error("Lootgroup: " + id + "@index: " + i + " (" + result.code.name() + ")");

                e.printStackTrace();

            }
            i++;
        }

        return new LootGroup(id, item, abstractLoots.toArray(new AbstractLoot[0]));
    }

    public static LootGroup fromOldConfig(String id) {

        FileConfiguration config = Main.getInstance().getConfig();

        ItemStack item;
        String tempPath;

        {
            tempPath = "gui.loot-group." + id + ".";
            String itemName = config.getString(tempPath + ".item");
            String name = config.getString(tempPath + ".name");
            List<String> lore = config.getStringList(tempPath + ".item");

            //this.name = id;
            item = ItemBuilder.builder(Material.valueOf(itemName)).name(name).lore(lore).toItem();

            //g = new LootGroup()
        }



        /*
            GENERICLOOT LOADING
         */
        ArrayList<AbstractLoot> abstractLoots = new ArrayList<>();

        boolean at_least = false;


        tempPath = "loot." + id + ".items";
        if (config.contains(tempPath)) {
            at_least = true;
            for (String itemKey : config.getConfigurationSection(tempPath).getKeys(false)) {
                String path = tempPath + "." + itemKey;

                Result result = new Result(null);

                try {
                    MemorySection instance = (MemorySection) config.get(path);
                    AbstractLoot abstractLoot = AbstractLoot.fromOldConfig(instance, result);

                    if (abstractLoot == null) {
                        Main.getInstance().error("Lootgroup: " + id + "@key: " + itemKey + " (" + result.code.name() + ")");
                        continue;
                    }

                    abstractLoots.add(abstractLoot);

                } catch (Exception e) {
                    Main.getInstance().error("Lootgroup: " + id + "@key: " + itemKey + " (" + result.code.name() + ")");

                    e.printStackTrace();
                }
            }
        } else {
            Main.getInstance().important("Couldnt find definition for lootgroup '" + id +
                    "' loot in config (is this intentional?)");
        }

        /*
            CRATELOOT LOADING
         */
        tempPath = "loot." + id + ".crates";
        if (config.isSet(tempPath)) {
            at_least = true;
            for (String crateKey : config.getConfigurationSection(tempPath).getKeys(false)) {

                if (!Main.crates.containsKey(crateKey)) {
                    Main.getInstance().error("Crate does not exist in config at " + tempPath + "." + crateKey);
                    continue;
                }
                /*
                    Crates will always safely be null due to not being loaded in yet
                 */

                Crate crate = Main.crates.get(crateKey);

                int c = config.getInt(tempPath + "." + crateKey + ".count");
                abstractLoots.add(new LootCrate(crate, c, c));
            }
        }

        if (!at_least) {
            Main.getInstance().error("Lootgroup '" + id + "' isn't correctly defined in config (no 'items' and no 'crates')" );
        }

        return new LootGroup(id, item, abstractLoots.toArray(new AbstractLoot[0]));
    }
}
