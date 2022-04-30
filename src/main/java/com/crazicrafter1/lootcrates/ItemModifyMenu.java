package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.gapi.SimpleMenu;
import com.crazicrafter1.gapi.TextMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

public class ItemModifyMenu extends SimpleMenu.SBuilder {

    public ItemModifyMenu() {
        super(2);
    }

    private static final String BASE64_CUSTOM_MODEL_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjU2NTJlYzMzYmI4YWJjNjMxNTA5M2Q1ZGZlMGYzNGQ0NzRjMjc3ZGE5YjBmMmE3MjZkNTA0ODY0ZTMxMDA5MyJ9fX0=";
    private static final String BASE64_ARROW = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDRmN2JjMWZhODIxN2IxOGIzMjNhZjg0MTM3MmEzZjdjNjAyYTQzNWM4MjhmYWE0MDNkMTc2YzZiMzdiNjA1YiJ9fX0=";

    private ItemBuilder builder;

    public ItemModifyMenu build(ItemStack it, Function<ItemStack, ItemStack> itemStackFunction) {
        builder = ItemBuilder.copyOf(it);
        return (ItemModifyMenu) title(p -> Lang.Edit_item)
                .background()
                .parentButton(0, 1)

                // descriptor text
                .button(7, 0, new Button.Builder()
                        .icon(p -> ItemBuilder.fromModernMaterial("BLACK_STAINED_GLASS_PANE").name("&7Raw colored item").lore("&6--->").build())
                )
                .button(7, 1, new Button.Builder()
                        .icon(p -> ItemBuilder.fromModernMaterial("BLACK_STAINED_GLASS_PANE").name("&7Fully <#519999>&bco&3lor&9ed</#786DBC> &7item").lore("&6--->").build())
                )

                // Completely inverted raw text
                .button(8, 0, new Button.Builder()
                        .icon(p -> {
                            String lore = ColorUtil.invertRendered(builder.getLoreString());
                            return builder.copy().name(builder.getName(), ColorUtil.INVERT_RENDERED, "" + ChatColor.GRAY)
                                // GRAY will only be applied to the first line
                                // How to fix this
                                .lore(lore != null ? ChatColor.GRAY + String.join("\n" + ChatColor.GRAY, lore.split("\n")) : null, ColorUtil.AS_IS)
                                .build();
                        })
                )
                // RENDER_ALL text
                .button(8, 1, new Button.Builder()
                        .icon(p -> builder.copy().renderAll().build())
                )

                .childButton(4, 0, p -> ItemBuilder.copyOf(Material.COMPASS).name("&8Set material").lore("&7Search...").build(), new TextMenu.TBuilder()
                        .title(p -> "Material search")
                        .leftRaw(p -> builder.getModernMaterial().toLowerCase())
                        .onClose((player) -> Result.PARENT())
                        .onComplete(((player, s, tBuilder) -> {
                            try {
                                //builder.material(builder.apply(builder));
                                // how to set material for legacy items
                                // set damage value
                                builder = ItemBuilder.copyOf(itemStackFunction.apply(builder.modernMaterial(s).build()));
                                return Result.PARENT();
                            } catch (Exception e) {
                                return Result.TEXT("Does not exist");
                            }
                        }))
                )

                .button(2, 0, new Button.Builder()
                        // if no custom name, don't even name
                        // just skip? or make an override to do nothing when null
                        //.icon(p -> builder.copy().name(builder.getName(), ColorUtil.RENDER_ALL, "" + ChatColor.GRAY).build())
                        .icon(p -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").name("&c&l[Item here]").build())
                        .lmb(interact -> {
                            if (interact.heldItem == null) {
                                return Result.MESSAGE(Lang.PLACE_ITEM);
                            }
                            builder = ItemBuilder.copyOf(itemStackFunction.apply(interact.heldItem));
                            return Result.REFRESH_GRAB(); // TODO this is untested
                        }))

                // Edit Name
                .childButton(3, 1, p -> ItemBuilder.copyOf(Material.NAME_TAG).name(Lang.NAME).lore(Lang.LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.NAME)
                        .leftRaw(p -> builder.getNameOrLocaleName())
                        .right(p -> Lang.SPECIAL_FORMATTING, p -> Editor.getColorDem(), ColorUtil.AS_IS)
                        .onClose((player) -> Result.PARENT())
                        .onComplete((player, s, b) -> {
                            if (s.isEmpty()) {
                                builder.removeName();
                            } else
                                builder.name(s, ColorUtil.RENDER_MARKERS);

                            builder = ItemBuilder.copyOf(itemStackFunction.apply(builder.build()));

                            return Result.PARENT();
                        }))
                // Edit Lore                                                                                // terrible name
                .childButton(4, 1, p -> ItemBuilder.copyOf(Material.GLOBE_BANNER_PATTERN).hideFlags(ItemFlag.HIDE_POTION_EFFECTS).name(Lang.LORE).lore(Lang.LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.LORE)
                        .leftRaw(p -> Util.def(builder.getLoreString(), Editor.LOREM_IPSUM).replace("\n", "\\n"))
                        .right(p -> Lang.SPECIAL_FORMATTING, p -> Editor.getColorDem(), ColorUtil.AS_IS)
                        .onClose((player) -> Result.PARENT())
                        .onComplete((player, s, b) -> {
                            if (s.isEmpty()) {
                                builder.removeLore();
                            } else
                                builder.lore(s.replace("\\n", "\n"), ColorUtil.RENDER_MARKERS);

                            builder = ItemBuilder.copyOf(itemStackFunction.apply(builder.build()));

                            return Result.PARENT();
                        }))
                // Edit CustomModelData
                .childButton(5, 1, p -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").skull(BASE64_CUSTOM_MODEL_DATA).name(Lang.CUSTOM_MODEL).lore(Lang.LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.CUSTOM_MODEL)
                        .leftRaw(p -> {
                            ItemMeta meta = builder.getMeta();
                            if (meta != null && meta.hasCustomModelData())
                                return "" + meta.getCustomModelData();
                            return Editor.LOREM_IPSUM;
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
