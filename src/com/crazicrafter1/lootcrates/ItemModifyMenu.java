package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ColorMode;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.crutils.Version;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.gapi.SimpleMenu;
import com.crazicrafter1.gapi.TextMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

import static com.crazicrafter1.lootcrates.Editor.*;
import static com.crazicrafter1.lootcrates.Lang.L;

public class ItemModifyMenu extends SimpleMenu.SBuilder {

    public ItemModifyMenu() {
        super(5);
    }

    public static final String BASE64_CUSTOM_MODEL_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQ2NDg3NGRmNDUyYzFkNzE3ZWRkZDBmYjNiODQ4MjAyYWQxNTU3MTI0NWFmNmZhZGUyZWNmNTE0ZjNjODBiYiJ9fX0=";

    private static final String Edit_item = "Edit item";
    private static final String Must_swap = "Must swap with an item";
    private static final String Name = "Name";
    private static final String Input_name = "Input custom item name";
    private static final String Input_lore = "Input custom item lore";
    private static final String Newline = "newline";
    private static final String None = "None";
    private static final String Input_integer = "Input an integer";

    private ItemBuilder builder;

    public ItemModifyMenu build(ItemStack it, Consumer<ItemStack> itemStackConsumer) {
        builder = ItemBuilder.copyOf(it);
        return (ItemModifyMenu) title(p -> L(p, Edit_item))
                .background()
                .parentButton(4, 4)
                .button(2, 1, IN_OUTLINE)
                .button(3, 2, IN_OUTLINE)
                .button(2, 3, IN_OUTLINE)
                .button(1, 2, IN_OUTLINE)
                // Edit ItemStack
                .button(2, 2, new Button.Builder()
                        .icon(p -> builder.build())
                        .lmb((interact) -> {
                            if (interact.heldItem == null) {
                                return Result.MESSAGE(L(interact.player, Must_swap));
                            }
                            builder = ItemBuilder.copyOf(interact.heldItem);

                            itemStackConsumer.accept(builder.build());

                            return Result.GRAB();
                        }))
                // Edit Name
                .childButton(6, 1, p -> ItemBuilder.copyOf(Material.NAME_TAG).name("&e" + L(p, Name)).lore("&7" + L(p, Lang.A.LMB) + ": &a" + L(p, Lang.A.Edit)).build(), new TextMenu.TBuilder()
                        .title(p -> L(p, Name))
                        .leftRaw(p -> builder.getNameOrLocaleName(), null, ColorMode.REVERT)
                        .right(p -> "&8" + L(p, Input_name), p -> ColorDem)
                        .onClose((player) -> Result.PARENT())
                        .onComplete((player, s, b) -> {
                            if (s.isEmpty()) {
                                builder.resetName();
                            } else
                                builder.name(s);

                            itemStackConsumer.accept(builder.build());

                            return Result.PARENT();
                        }))
                // Edit Lore
                .childButton(6, 3, p -> ItemBuilder.copyOf(Material.MAP).name("&7" + L(p, Lang.A.Lore)).lore("&7" + L(p, Lang.A.LMB) + ": &a" + L(p, Lang.A.Edit)).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.A.Lore)
                        .leftRaw(p -> Util.strDef(builder.getLoreString(), Lang.A.Lorem_ipsum), null, ColorMode.REVERT)
                        .right(p -> "&8" + L(p, Input_lore), p -> ColorDem + "&7\n'\\n': &f" + L(p, Newline))
                        .onClose((player) -> Result.PARENT())
                        .onComplete((player, s, b) -> {
                            if (s.isEmpty()) {
                                builder.resetLore();
                            } else
                                builder.lore(s.replace("\\n", "\n"));

                            itemStackConsumer.accept(builder.build());

                            return Result.PARENT();
                        }))
                // Edit CustomModelData
                .childButton(6, 2, p -> ItemBuilder.of("PLAYER_HEAD").skull(BASE64_CUSTOM_MODEL_DATA).name("&8" + L(p, Lang.A.Custom_model_data)).lore("&7" + L(p, Lang.A.LMB) + ": &a" + L(p, Lang.A.Edit)).build(), new TextMenu.TBuilder()
                        .title(p -> L(p, Lang.A.Custom_model_data))
                        .leftRaw(p -> {
                            ItemMeta meta = builder.getMeta();
                            if (meta != null && meta.hasCustomModelData())
                                return "" + meta.getCustomModelData();
                            return L(p, None);
                        })
                        .right(p -> "&7" + L(p, Input_integer))
                        .onClose((player) -> Result.PARENT())
                        .onComplete((p, s, b) -> {
                            if (s.isEmpty())
                                return null;

                            int i;
                            try {
                                i = Integer.parseInt(s);
                            } catch (Exception e00) {
                                return Result.TEXT(L(p, Lang.A.Invalid));
                            }
                            builder.model(i);

                            itemStackConsumer.accept(builder.build());

                            return Result.PARENT();
                        }), Version.AT_LEAST_v1_16.a());

    }

}
