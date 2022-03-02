package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.gapi.SimpleMenu;
import com.crazicrafter1.gapi.TextMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

import static com.crazicrafter1.lootcrates.Editor.*;

public class ItemModifyMenu extends SimpleMenu.SBuilder {

    public ItemModifyMenu() {
        super(5);
    }

    public static final String BASE64_CUSTOM_MODEL_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQ2NDg3NGRmNDUyYzFkNzE3ZWRkZDBmYjNiODQ4MjAyYWQxNTU3MTI0NWFmNmZhZGUyZWNmNTE0ZjNjODBiYiJ9fX0=";

    private ItemBuilder builder;

    public ItemModifyMenu build(ItemStack it, Function<ItemStack, ItemStack> itemStackFunction) {
        builder = ItemBuilder.copyOf(it);
        return (ItemModifyMenu) title(p -> Lang.Edit_item)
                .background()
                .parentButton(4, 4)
                .button(2, 1, IN_OUTLINE)
                .button(3, 2, IN_OUTLINE)
                .button(2, 3, IN_OUTLINE)
                .button(1, 2, IN_OUTLINE)
                .button(1, 1, new Button.Builder()
                        .icon(p -> builder.copy().name(builder.getName(), ColorMode.INVERT_RENDERED)
                                .lore(builder.getLoreString(), ColorMode.INVERT_RENDERED)
                                .build())
                )
                .button(1, 3, new Button.Builder()
                        .icon(p -> builder.copy().renderAll().build())
                )
                // Edit ItemStack
                .button(2, 2, new Button.Builder()
                        .icon(p -> builder.copy().build())
                        .lmb((interact) -> {
                            if (interact.heldItem == null) {
                                return Result.MESSAGE(Lang.Must_swap);
                            }
                            builder = ItemBuilder.copyOf(interact.heldItem);

                            // reapply a new item result for fancy consistency
                            builder = ItemBuilder.copyOf(itemStackFunction.apply(builder.build()));

                            return Result.GRAB();
                        }))
                // Edit Name
                .childButton(6, 1, p -> ItemBuilder.copyOf(Material.NAME_TAG).name("&e" + Lang.Name).lore(Lang.LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.Name)
                        .leftRaw(p -> builder.getNameOrLocaleName(), ColorMode.INVERT_RENDERED, null, ColorMode.AS_IS)
                        .right(p -> "Special formatting", ColorMode.AS_IS, p -> COLORS, ColorMode.AS_IS)
                        .onClose((player) -> Result.PARENT())
                        .onComplete((player, s, b) -> {
                            if (s.isEmpty()) {
                                builder.resetName();
                            } else
                                builder.name(s);

                            builder = ItemBuilder.copyOf(itemStackFunction.apply(builder.build()));

                            return Result.PARENT();
                        }))
                // Edit Lore
                .childButton(6, 3, p -> ItemBuilder.copyOf(Material.MAP).name(Lang.EDIT_LORE).lore(Lang.LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.EDIT_LORE)
                        .leftRaw(p -> Util.def(builder.getLoreString(), Lang.LOREM_IPSUM).replace("\n", "\\n"), ColorMode.INVERT_RENDERED, null, ColorMode.AS_IS)
                        .right(p -> "Special formatting", ColorMode.AS_IS, p -> COLORS, ColorMode.AS_IS)
                        .onClose((player) -> Result.PARENT())
                        .onComplete((player, s, b) -> {
                            if (s.isEmpty()) {
                                builder.resetLore();
                            } else
                                builder.lore(s.replace("\\n", "\n"));

                            builder = ItemBuilder.copyOf(itemStackFunction.apply(builder.build()));

                            return Result.PARENT();
                        }))
                // Edit CustomModelData
                .childButton(6, 2, p -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").skull(BASE64_CUSTOM_MODEL_DATA).name("&8" + Lang.CUSTOM_MODEL).lore(Lang.LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.CUSTOM_MODEL)
                        .leftRaw(p -> {
                            ItemMeta meta = builder.getMeta();
                            if (meta != null && meta.hasCustomModelData())
                                return "" + meta.getCustomModelData();
                            return Lang.LOREM_IPSUM;
                        })
                        .right(p -> "&7" + Lang.Input_integer)
                        .onClose((player) -> Result.PARENT())
                        .onComplete((p, s, b) -> {
                            if (s.isEmpty())
                                return null;

                            int i;
                            try {
                                i = Integer.parseInt(s);
                            } catch (Exception e00) {
                                return Result.TEXT(Lang.ERR_INVALID);
                            }
                            builder.model(i);

                            builder = ItemBuilder.copyOf(itemStackFunction.apply(builder.build()));

                            return Result.PARENT();
                        }), Version.AT_LEAST_v1_16.a());

    }

}
