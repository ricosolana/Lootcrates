package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Legacy data configurator
 */
public class Data implements ConfigurationSerializable {
    private static final int REV_LATEST = 1;

    public long cleanAfterDays;
    public boolean update;
    public int speed;

    public ItemStack unSelectedItem;
    public ItemStack selectedItem;
    public FireworkEffect fireworkEffect;

    public LinkedHashMap<String, Crate> crates;
    public LinkedHashMap<String, LootSet> lootSets;

    /**
     * Default constructor in the case of a normal config not being available
     */
    public Data() {
        cleanAfterDays = 7; // Cleanup config files older than a week
        update = true;
        speed = 4;
        unSelectedItem = ItemBuilder.copyOf(Material.CHEST).name("&f&l???").lore("&7Choose 4 mystery chests, and\n&7your loot will be revealed!").build();
        selectedItem = ItemBuilder.of("WHITE_STAINED_GLASS_PANE").name("&7&l???").lore("&7You have selected this mystery chest").build();

        lootSets = new LinkedHashMap<>();
        LootSet lootSet = new LootSet(
                "common",
                ItemBuilder.of("WHITE_STAINED_GLASS_PANE").name("&f&lCommon Reward").build(),
                new ArrayList<>(Collections.singletonList(new LootItem())));
        lootSets.put("common", lootSet);

        crates = new LinkedHashMap<>();
        Crate crate = new Crate("peasant",
                ItemBuilder.copyOf(Material.CHEST).name("&f&lPeasant Crate").build(),
                "select loot",
                3,
                4,
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        crates.put("peasant", crate);

        crate.lootByWeight = new HashMap<>();
        crate.lootByWeight.put(lootSet, 10);
        crate.weightsToSums();

        fireworkEffect = FireworkEffect.builder().withColor(Color.RED, Color.BLUE, Color.WHITE).with(FireworkEffect.Type.BURST).build();
    }

    public Data(Map<String, Object> args) {
        int rev = (int) args.getOrDefault("rev", 0); // for legacy unmarked revisions

        if (rev == 0) {
            // 2/20/2022 and before
            cleanAfterDays = (int) args.getOrDefault("cleanHour", 7) / 24;
            Main.get().lang.translate = (boolean) args.getOrDefault("translate", false);
        } else if (rev == 1) {
            // after 2/20/22
            cleanAfterDays = (int) args.getOrDefault("cleanAfterDays", 7);
        }

        update = (boolean) args.getOrDefault("update", true);
        speed = (int) args.getOrDefault("speed", 4);

        unSelectedItem = (ItemStack) args.get("unSelectedItem");
        selectedItem = (ItemStack) args.get("selectedItem");

        // load in the same way, but need to pass name somehow
        lootSets = (LinkedHashMap<String, LootSet>) args.get("lootSets");
        for (Map.Entry<String, LootSet> entry : lootSets.entrySet()) {
            entry.getValue().id = entry.getKey();
        }

        crates = (LinkedHashMap<String, Crate>) args.get("crates");
        for (Map.Entry<String, Crate> entry : crates.entrySet()) {
            String id = entry.getKey();
            Crate crate = entry.getValue();

            crate.id = id;
            crate.itemStack = LootCratesAPI.makeCrate(crate.itemStack, id);

            // initialize weights
            crate.sumsToWeights();
        }

        fireworkEffect = (FireworkEffect) args.get("fireworkEffect");
    }

    public ItemStack unSelectedItem(Player p) {
        Lang.Unit dlu = Main.get().lang.getUnit(p);

        if (dlu == null) {
            return unSelectedItem;
        }

        return ItemBuilder.copyOf(unSelectedItem)
                .name(dlu.unSelectedDisplayName)
                .lore(dlu.unSelectedLore)
                .build();
    }

    public ItemStack selectedItem(Player p) {
        Lang.Unit dlu = Main.get().lang.getUnit(p);

        if (dlu == null) {
            return selectedItem;
        }

        return ItemBuilder.copyOf(selectedItem)
                .name(dlu.selectedDisplayName)
                .lore(dlu.selectedLore)
                .build();
    }

    @Override
    public final Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("rev", REV_LATEST);
        result.put("cleanAfterDays", cleanAfterDays);
        //result.put("translate", translate);
        result.put("update", update);
        result.put("speed", speed);

        result.put("unSelectedItem", unSelectedItem);
        result.put("selectedItem", selectedItem);

        result.put("lootSets", lootSets);

        result.put("crates", crates);

        result.put("fireworkEffect", fireworkEffect);

        return result;
    }
}
