package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.MathUtil;
import com.crazicrafter1.crutils.RandomUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.crutils.ui.AbstractMenu;
import com.crazicrafter1.lootcrates.*;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

public class LootItem extends AbstractLootItem {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.GOLD_NUGGET).name("&6Add item...").build();

    public ItemStack item;

    /**
     * Default ctor
     */
    public LootItem() {
        //noinspection ConstantConditions
        this.item = ItemBuilder.copy(RandomUtil.getRandom(Arrays.asList(Material.DIAMOND_PICKAXE, Material.GOLDEN_SWORD, Material.IRON_AXE))).build();
    }

    // todo remove post
    public LootItem(ItemStack itemStack) {
        this.item = itemStack;
    }

    public LootItem(Map<String, Object> args) {
        super(args);

        // TODO eventually remove older revisions
        int rev = Main.get().rev;
        if (rev < 2)
            this.item = (ItemStack) args.get("itemStack");
        else if (rev < 6)
            this.item = ((ItemBuilder) args.get("item")).build();
        else
            this.item = (ItemStack) args.get("item");

        if (item == null) {
            Main.get().notifier.severe("A LootItem is null in config");
        }
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return super.ofRange(p, item);
    }

    @Nonnull
    @Override
    public ItemStack getMenuIcon() {
        // set count if min==max
        return ItemBuilder.copy(item).amount(min == max ? min : 1).build();
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("item", item);
        return result;
    }

    @Nonnull
    @Override
    public ItemModifyMenu getMenuBuilder() {
        return (ItemModifyMenu) rangeButtons(new ItemModifyMenu()
                .build(item, input -> this.item = input),
                item, 3, 0, 5, 0);
    }
}
