package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.WeightedRandomContainer;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import com.crazicrafter1.lootcrates.crate.LootSetSettings;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.apache.commons.lang.Validate;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class RewardSettings {
    public int speed;

    public ItemStack unSelectedItem;
    public ItemStack selectedItem;
    public FireworkEffect fireworkEffect;

    public Map<String, CrateSettings> crates;
    public Map<String, LootSetSettings> lootSets;

    public RewardSettings(int speed, ItemStack unSelectedItem, ItemStack selectedItem, FireworkEffect fireworkEffect, Map<String, CrateSettings> crates, Map<String, LootSetSettings> lootSets) {
        this.speed = speed;
        this.unSelectedItem = unSelectedItem;
        this.selectedItem = selectedItem;
        this.fireworkEffect = fireworkEffect;
        this.crates = crates;
        this.lootSets = lootSets;
    }

    /**
     * fallback
     */
    public RewardSettings() {
        speed = 4;
        unSelectedItem = ItemBuilder.copy(Material.CHEST).name("&f&l???").lore("&7&oChoose 4 mystery chests, and\n&7&oyour loot will be revealed!").build();
        selectedItem = ItemBuilder.from("WHITE_STAINED_GLASS_PANE").name("&7&l???").lore("&7You have selected this mystery chest").build();

        lootSets = new LinkedHashMap<>();
        LootSetSettings lootSet = new LootSetSettings(
                "common",
                ItemBuilder.from("WHITE_STAINED_GLASS_PANE").name("&f&lCommon Reward").build(),
                new ArrayList<>(Collections.singletonList(new LootItem())));
        lootSets.put(lootSet.id, lootSet);

        crates = new LinkedHashMap<>();
        CrateSettings crate = new CrateSettings("peasant");
        crates.put(crate.id, crate);

        crate.loot.add(lootSet, 10);

        fireworkEffect = FireworkEffect.builder().withColor(Color.RED, Color.BLUE, Color.WHITE).with(FireworkEffect.Type.BURST).build();
    }

    public RewardSettings(ConfigurationSection section) {
        //Map<String, Object> args = section.getValues(false);
        try {
            this.unSelectedItem = Objects.requireNonNull(section.getItemStack("unSelectedItem"), String.format(Lang.CONFIG_NULL_VALUE, "unSelectedItem"));
            this.selectedItem = Objects.requireNonNull(section.getItemStack("selectedItem"), String.format(Lang.CONFIG_NULL_VALUE, "selectedItem"));

            this.lootSets = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : section.getConfigurationSection("lootSets").getValues(false).entrySet()) {
                Map<String, Object> itr = ((ConfigurationSection) entry.getValue()).getValues(false);

                String id = entry.getKey();
                ItemStack itemStack = Objects.requireNonNull((ItemStack) itr.get("item"), String.format(Lang.CONFIG_NULL_VALUE, "'lootSets.<" + id + ">.item'"));

                List<ILoot> loot = (List<ILoot>) itr.get("loot");
                Validate.isTrue(!loot.contains(null), String.format(Lang.CONFIG_NULL_VALUE, "'lootSets.<" + id + ">.loot[]'"));

                this.lootSets.put(id, new LootSetSettings(id, itemStack, loot));
            }

            this.crates = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : section.getConfigurationSection("crates").getValues(false).entrySet()) {
                Map<String, Object> itr = ((ConfigurationSection) entry.getValue()).getValues(false);
                String id = entry.getKey();

                Map<String, Object> weights = ((ConfigurationSection) itr.get("weights")).getValues(false);
                Map<LootSetSettings, Integer> refWeights = weights.entrySet().stream()
                        .collect(Collectors.toMap(e -> Objects.requireNonNull(this.lootSets.get(e.getKey()), e.getKey() + "missing reference to LootSet '" + e.getKey() + "' in 'crates.<" + id + ">.weights.<" + e.getKey() + ">'"),
                                e -> (Integer) e.getValue()));//

                this.crates.put(id, new CrateSettings(id,
                        Objects.requireNonNull((String) itr.get("title"), String.format(Lang.CONFIG_NULL_VALUE, "'crates.<" + id + ">.title'")),
                        (int) itr.get("columns"),
                        (int) itr.get("picks"),
                        Sound.valueOf((String) itr.get("sound")),
                        new WeightedRandomContainer<>(refWeights),
                        LootcratesAPI.getCrateAsItem(Objects.requireNonNull((ItemStack) itr.get("item"), String.format(Lang.CONFIG_NULL_VALUE, "'crates.<" + id + ">.item'")), id)));
            }

            fireworkEffect = (FireworkEffect) section.get("fireworkEffect");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void serialize(ConfigurationSection section) {
        try {
            section.set("speed", speed);
            section.set("speed", speed);

            section.set("unSelectedItem", unSelectedItem);
            section.set("selectedItem", selectedItem);

            lootSets.forEach((k, v) -> v.serialize(section.createSection("lootSets." + k)));
            crates.forEach((k, v) -> v.serialize(section.createSection("crates." + k)));

            section.set("fireworkEffect", fireworkEffect);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public ItemStack unSelectedItemStack(@Nonnull Player p, @Nonnull CrateSettings crate) {
        return ItemBuilder.copy(unSelectedItem)
                .replace("crate_picks", "" + crate.picks, '%')
                .placeholders(p)
                .renderAll()
                .build();
    }

    public ItemStack selectedItemStack(@Nonnull Player p, @Nonnull CrateSettings crate) {
        return ItemBuilder.copy(selectedItem)
                .replace("crate_picks", "" + crate.picks, '%')
                .placeholders(p)
                .renderAll()
                .build();
    }
}
