package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.ItemBuilder;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.loot.LootCrate;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LootGroup {

    private final String name;
    private final ItemStack itemStack;
    //private ArrayList<String> items; // replace this
    private final AbstractLoot[] loot; // array of multiple loots

    private static Main plugin;
    private static FileConfiguration config;

    public static void onEnable(Main plugin) {
        LootGroup.plugin = plugin;
        config = plugin.getConfig();
    }

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

    public static LootGroup fromOldConfig(String id) {

        ItemStack icon;
        String tempPath;

        {
            tempPath = "gui.loot-group." + id + ".";
            String item = config.getString(tempPath + ".item");
            String name = config.getString(tempPath + ".name");
            List<String> lore = config.getStringList(tempPath + ".item");

            //this.name = id;
            icon = ItemBuilder.builder(Material.valueOf(item)).name(name).lore(lore).toItem();

            //g = new LootGroup()
        }



        /*
            LOOT LOADING
         */
        ArrayList<AbstractLoot> abstractLoots = new ArrayList<>();


        tempPath = "loot." + id + ".items";
        if (config.isSet(tempPath))
            for (String itemKey : config.getConfigurationSection(tempPath).getKeys(false)) {
                String path = tempPath + "." + itemKey;

                try {
                    Map<String, Object> instance = (Map<String, Object>) config.get(path);
                    AbstractLoot abstractLoot = AbstractLoot.fromOldConfig(instance);

                    if (abstractLoot != null)
                        abstractLoots.add(abstractLoot);
                    else plugin.error("While reading config, could not load loot at " + path);

                } catch (Exception e) {
                    plugin.error("Possible config structuring issue located at " + path);
                    plugin.error(e.getMessage());
                }
            }


        /*
            CRATE LOOT LOADING
         */
        tempPath = "loot." + id + ".crates";
        if (config.isSet(tempPath)) {
            for (String crateKey : config.getConfigurationSection(tempPath).getKeys(false)) {

                Crate crate = Main.crates.getOrDefault(crateKey, null);
                if (crate == null) {
                    plugin.error("Crate does not exist in config at " + tempPath + "." + crateKey);
                    continue;
                }
                int c = config.getInt(tempPath + "." + crateKey + ".count");
                abstractLoots.add(new LootCrate(crate, c, c));
            }
        }

        return new LootGroup(id, icon, (AbstractLoot[]) abstractLoots.toArray());
    }
}
