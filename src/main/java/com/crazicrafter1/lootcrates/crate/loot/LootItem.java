package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.MathUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.lootcrates.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;

public final class LootItem extends AbstractLootItem {

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
        else if (rev == 2)
            this.item = ((ItemBuilder) args.get("item"));
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return super.ofRange(p, item.build());
    }

    @Nonnull
    @Override
    public ItemStack getMenuIcon(@Nonnull Player p) {
        return item.build();
    }

    @NotNull
    @Override
    public String getMenuDesc(@NotNull Player p) {

        return super.getMenuDesc(p);
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
    public AbstractMenu.Builder getMenuBuilder() {
        //Button.Builder inOutline = new Button.Builder().icon(() -> new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&7Set to").toItem());
        return new ItemModifyMenu()
                .build(item.build(), input -> (this.item = ItemBuilder.mutable(input)).build())
                // Min
                .button(5, 2, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            min = MathUtil.clamp(min - change, 1, min);
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            min = MathUtil.clamp(min + change, 1, max);
                            return Result.REFRESH();
                        })
                        .icon(p -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").name(Lang.MINIMUM).skull(Editor.BASE64_DEC).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC + "\n" + Lang.SHIFT_MUL).amount(min).build()))
                // Max
                .button(7, 2, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = MathUtil.clamp(max - change, min, item.getMaxSize());
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = MathUtil.clamp(max + change, min, item.getMaxSize());
                            return Result.REFRESH();
                        })
                        .icon(p -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").name(Lang.MAXIMUM).skull(Editor.BASE64_INC).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC + "\n" + Lang.SHIFT_MUL).amount(max).build()));
    }
}
