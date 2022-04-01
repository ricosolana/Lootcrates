package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.MathUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.lootcrates.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;

public class LootItem extends AbstractLootItem {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.GOLD_NUGGET).name("&6Add item...").build();

    public ItemBuilder item;

    /**
     * Default ctor
     */
    public LootItem() {
        this.item = ItemBuilder.copyOf(Material.STONE);
    }

    public LootItem(Map<String, Object> args) {
        super(args);

        int rev = Main.get().rev;
        if (rev < 2)
            this.item = ItemBuilder.mutable((ItemStack) args.get("itemStack"));
        else
            this.item = ((ItemBuilder) args.get("item"));
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return super.ofRange(p, item.build());
    }

    @Nonnull
    @Override
    public ItemStack getMenuIcon() {
        // set count if min==max
        return item.copy().amount(min == max ? min : 1).build();
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
                .build(item.build(), input -> (this.item = ItemBuilder.mutable(input)).build()),
                item.build(), 3, 0, 5, 0);
    }
}
