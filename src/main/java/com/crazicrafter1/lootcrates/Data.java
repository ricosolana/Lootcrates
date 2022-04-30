package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Legacy data configurator
 */
public class Data implements ConfigurationSerializable {

    //public long cleanAfterDays;
    public int speed;

    public ItemBuilder unSelectedItem;
    public ItemBuilder selectedItem;
    public FireworkEffect fireworkEffect;

    public LinkedHashMap<String, Crate> crates;
    public LinkedHashMap<String, LootSet> lootSets;

    /**
     * Default constructor in the case of a normal config not being available
     */
    public Data() {
        //cleanAfterDays = 7; // Cleanup config files older than a week
        speed = 4;
        unSelectedItem = ItemBuilder.copyOf(Material.CHEST).name("&f&l???").lore("&7&oChoose 4 mystery chests, and\n&7&oyour loot will be revealed!");
        selectedItem = ItemBuilder.fromModernMaterial("WHITE_STAINED_GLASS_PANE").name("&7&l???").lore("&7You have selected this mystery chest");

        lootSets = new LinkedHashMap<>();
        LootSet lootSet = new LootSet(
                "common",
                ItemBuilder.fromModernMaterial("WHITE_STAINED_GLASS_PANE").name("&f&lCommon Reward").build(),
                new ArrayList<>(Collections.singletonList(new LootItem())));
        lootSets.put(lootSet.id, lootSet);

        crates = new LinkedHashMap<>();
        Crate crate = new Crate("peasant");
        crates.put(crate.id, crate);

        crate.loot.add(lootSet, 10);

        fireworkEffect = FireworkEffect.builder().withColor(Color.RED, Color.BLUE, Color.WHITE).with(FireworkEffect.Type.BURST).build();
    }

    public Data(Map<String, Object> args) {
        try {
            int rev = Main.get().rev;

            // TODO eventually remove older revisions
            if (rev == 0) {
                // 2/20/2022 and before
                Main.get().cleanAfterDays = (int) args.getOrDefault("cleanHour", 7) / 24;
            } else if (rev <= 2) {
                // after 2/20/22
                Main.get().cleanAfterDays = (int) args.getOrDefault("cleanAfterDays", 7);
            }

            speed = (int) args.getOrDefault("speed", 4);

            if (rev < 2) {
                unSelectedItem = ItemBuilder.mutable((ItemStack) args.get("unSelectedItem"));
                selectedItem = ItemBuilder.mutable((ItemStack) args.get("selectedItem"));
            } else {
                // after 3/2/22
                unSelectedItem = ((ItemBuilder) args.get("unSelectedItem"));
                selectedItem = ((ItemBuilder) args.get("selectedItem"));
            }

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
                crate.item = ItemBuilder.mutable(LootCratesAPI.makeCrate(crate.item.build(), id));

                // initialize weights
                //crate.sumsToWeights();
            }

            fireworkEffect = (FireworkEffect) args.get("fireworkEffect");
        } catch (Exception e) {
            Main.get().error("Failed to load config: " + e.getMessage());
            Main.get().error("You can try to fix this manually (good luck) or reset the config with </crates reset>");
            e.printStackTrace();
        }
    }

    public ItemStack unSelectedItemStack(@Nonnull Player p, @Nonnull Crate crate) {
        return unSelectedItem.copy()
                .replace("crate_picks", "" + crate.picks, '%')
                .placeholders(p)
                .renderAll()
                .build();
    }

    public ItemStack selectedItemStack(@Nonnull Player p, @Nonnull Crate crate) {
        return selectedItem.copy()
                .replace("crate_picks", "" + crate.picks, '%')
                .placeholders(p)
                .renderAll()
                .build();
    }

    @Override
    public final Map<String, Object> serialize() {
        try {
            Map<String, Object> result = new LinkedHashMap<>();

            //result.put("cleanAfterDays", cleanAfterDays);
            result.put("speed", speed);

            result.put("unSelectedItem", unSelectedItem);
            result.put("selectedItem", selectedItem);

            result.put("lootSets", lootSets);

            result.put("crates", crates);

            result.put("fireworkEffect", fireworkEffect);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
