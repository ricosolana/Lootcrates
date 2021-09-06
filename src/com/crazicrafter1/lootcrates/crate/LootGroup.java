package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.util.ItemBuilder;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.util.Util;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public record LootGroup(String name, ItemStack itemStack,
                        ArrayList<AbstractLoot> loot) {

    public AbstractLoot getRandomLoot() {
        return loot.get((int) (Math.random() * loot.size()));
    }

    public static LootGroup fromConfig(String id) {

        FileConfiguration config = Main.getInstance().getConfig();

        ItemStack item;
        String tempPath;

        {
            tempPath = "gui.lootgroup." + id + ".";
            String itemName = config.getString(tempPath + ".icon");
            String name = config.getString(tempPath + ".title");
            List<String> lore = config.getStringList(tempPath + ".footer");

            item = ItemBuilder.builder(Util.getCompatibleItem(itemName)).name(name).lore(lore).toItem();
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

            EnumParseResult result = new EnumParseResult(null);
            try {

                AbstractLoot abstractLoot = AbstractLoot.fromNewConfig(((Map<String, Object>) map), result);

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

        return new LootGroup(id, item, abstractLoots);
    }
}
