package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.crutils.Util;
import org.bukkit.Material;
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

            item = new ItemBuilder(Material.matchMaterial(itemName)).name(name).lore(lore).toItem();
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

            AbstractLoot abstractLoot = AbstractLoot.fromNewConfig(((Map<String, Object>) map), result);

            if (abstractLoot == null) {
                Main.getInstance().error("Loot: " + id + "@index: " + i + " (" + result.code.name() + ")");
            } else
                abstractLoots.add(abstractLoot);

            i++;
        }

        return new LootGroup(id, item, abstractLoots);
    }
}
