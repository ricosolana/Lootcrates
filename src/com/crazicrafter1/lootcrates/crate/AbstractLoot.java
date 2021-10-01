package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.Bool;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.loot.LootItemCrate;
import com.crazicrafter1.lootcrates.crate.loot.LootItemQA;
import com.crazicrafter1.lootcrates.crate.loot.LootOrdinateItem;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *  Abstract loot class is more or less abstracted config data
 *
 *  ActiveLoot is a loot that has calculated values upon being revealed
 *      - what to show on reveal
 *      - what to do on click
 */
public abstract class AbstractLoot implements ConfigurationSerializable {

    private final ItemStack icon;

    public AbstractLoot(ItemStack baseVisual) {
        this.icon = baseVisual;
    }

    public ItemStack getIcon() {
        return icon;
    }

    /*
     * do nothing by default
     */
    public abstract void execute(ActiveCrate activeCrate, boolean closed, Bool giveItem);


}
