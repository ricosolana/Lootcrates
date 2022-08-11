package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.LootSetSettings;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Legacy data configurator
 */
@Deprecated
public class Data implements ConfigurationSerializable {
    public int speed;

    public ItemBuilder unSelectedItem;
    public ItemBuilder selectedItem;
    public FireworkEffect fireworkEffect;

    public LinkedHashMap<String, Crate> crates;
    public LinkedHashMap<String, LootSet> lootSets;

    public Data(Map<String, Object> args) {
        try {
            int rev = Main.get().rev;

            // TODO remove post-migrate
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
            }

            fireworkEffect = (FireworkEffect) args.get("fireworkEffect");
        } catch (Exception e) {
            Main.get().notifier.severe("Failed to load config: " + e.getMessage());
            Main.get().notifier.severe("Possible issues/solutions: ");
            Main.get().notifier.severe(" - There are null values in config: ");
            Main.get().notifier.severe("     - This is likely caused by a prior plugin failure, try using an earlier backup");
            Main.get().notifier.severe(" - Chained errors when config being loaded: ");
            Main.get().notifier.severe("     - There might be null values in config, try the above");
            Main.get().notifier.severe(" - Material does not exist when config is being loaded: ");
            Main.get().notifier.severe("     - You are on a version that might not be supported by lootcrates, or the material is named badly");
            Main.get().notifier.severe("You can try to fix the config manually/restoring a backup, resetting the config with </crates reset>, or by doing </crates populate>");
            e.printStackTrace();
        }
    }

    @Override
    public final Map<String, Object> serialize() {
        throw new UnsupportedOperationException("Do not use!");
    }

    //todo remove post-migrate
    public RewardSettings getSettings() {
        Map<String, CrateSettings> crates = this.crates.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSettings()));
        Map<String, LootSetSettings> loot = this.lootSets.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSettings()));

        return new RewardSettings(speed, unSelectedItem.build(), selectedItem.build(), fireworkEffect, crates, loot);
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
}
