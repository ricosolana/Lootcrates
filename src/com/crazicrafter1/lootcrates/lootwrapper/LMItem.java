package com.crazicrafter1.lootcrates.lootwrapper;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.EnumResult;
import com.crazicrafter1.gapi.SimpleMenu;
import com.crazicrafter1.gapi.TextMenu;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class LMItem extends LMWrapper {
    public LMItem(final LootItem lootItem, final LootSet lootSet) {
        Button.Builder b = new Button.Builder().icon(() -> new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&7Set to").toItem());
        menu = new SimpleMenu.SBuilder(5)
                .title(ChatColor.DARK_GRAY + "LootItem editor")
                .parentButton(4, 4)
                .button(2, 1, b)
                .button(3, 2, b)
                .button(2, 3, b)
                .button(1, 2, b)
                .background()
                // ItemStack
                .button(2, 2, new Button.Builder()
                        .icon(() -> lootItem.itemStack)
                        .lmb(interact -> EnumResult.GRAB_ITEM))
                // Edit name
                .childButton(6, 1, () -> new ItemBuilder(Material.NAME_TAG).name("&eName").lore("&8LMB: &aEdit").toItem(), new TextMenu.TBuilder()
                        .title("&8Edit name")
                        .onClose(player -> EnumResult.BACK)
                        .onComplete((player, s) -> {
                            if (s.isEmpty())
                                return EnumResult.OK;
                            lootItem.itemStack = new ItemBuilder(lootItem.itemStack).name(s).toItem();
                            return EnumResult.BACK;
                        }))
                // Min
                .button(5, 2, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.isShift() ? 5 : 1;
                            lootItem.min = Util.clamp(lootItem.min - change, 1, lootItem.min);
                            return EnumResult.REFRESH;
                        })
                        .rmb(interact -> {
                            int change = interact.isShift() ? 5 : 1;
                            lootItem.min = Util.clamp(lootItem.min + change, 1, lootItem.max);
                            return EnumResult.REFRESH;
                        })
                        .icon(() -> new ItemBuilder(Material.MEDIUM_AMETHYST_BUD).name("&c-").count(lootItem.min).toItem()))
                // Max
                .button(7, 2, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.isShift() ? 5 : 1;
                            lootItem.max = Util.clamp(lootItem.max - change, lootItem.min, lootItem.itemStack.getMaxStackSize());
                            return EnumResult.REFRESH;
                        })
                        .rmb(interact -> {
                            int change = interact.isShift() ? 5 : 1;
                            lootItem.max = Util.clamp(lootItem.max + change, lootItem.min, lootItem.itemStack.getMaxStackSize());
                            return EnumResult.REFRESH;
                        })
                        .icon(() -> new ItemBuilder(Material.AMETHYST_CLUSTER).name("&a+").count(lootItem.max).toItem()))
                // Edit lore
                .childButton(6, 3, () -> new ItemBuilder(Material.MAP).name("&7Lore").lore("&8LMB: &aEdit").toItem(), new TextMenu.TBuilder()
                        .title("&8Item lore")
                        .onClose(player -> EnumResult.BACK)
                        .onComplete((player, s) -> {
                            if (s.isEmpty())
                                return EnumResult.OK;
                            lootItem.itemStack = new ItemBuilder(lootItem.itemStack).lore(s).toItem();
                            return EnumResult.BACK;
                        }))
                .validate();
    }
}
