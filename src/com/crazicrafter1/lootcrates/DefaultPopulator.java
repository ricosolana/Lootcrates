package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DefaultPopulator {

    public static void populate() {
        Main.get().data = new Data();

        Data data = Main.get().data;

        data.debug = false;
        data.update = true;
        data.speed = 4;
        data.unSelectedItem = new ItemBuilder(Material.CHEST).name("&f&l???").lore("&7Choose 4 mystery chests, and\n&7your loot will be revealed!").toItem();
        data.selectedItem = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name("&7&l???").lore("&7You have selected this mystery chest").toItem();

        data.lootSets = new HashMap<>();
        LootSet lootSet = new LootSet(
                "common",
                new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name("&f&lCommon Reward").toItem(),
                new ArrayList<>(Collections.singletonList(new LootItem())));
        data.lootSets.put("common", lootSet);

        data.crates = new HashMap<>();
        Crate crate = new Crate("peasant",
                new ItemBuilder(Material.CHEST).name("&f&lPeasant Crate").toItem(),
                "select loot",
                3,
                4,
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        data.crates.put("peasant", crate);

        crate.lootByWeight = new HashMap<>();
        crate.lootByWeight.put(data.lootSets.get("common"), 10);
        crate.weightsToSums();

        data.fireworkEffect = FireworkEffect.builder().withColor(Color.RED, Color.BLUE, Color.WHITE).with(FireworkEffect.Type.BURST).build();
    }

}
