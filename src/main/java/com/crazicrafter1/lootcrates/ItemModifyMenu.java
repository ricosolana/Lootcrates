package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.crutils.ui.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

public class ItemModifyMenu extends SimpleMenu.SBuilder {

    public ItemModifyMenu() {
        super(5);
    }

    private static final String BASE64_CUSTOM_MODEL_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjU2NTJlYzMzYmI4YWJjNjMxNTA5M2Q1ZGZlMGYzNGQ0NzRjMjc3ZGE5YjBmMmE3MjZkNTA0ODY0ZTMxMDA5MyJ9fX0=";
    //private static final String BASE64_ARROW = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDRmN2JjMWZhODIxN2IxOGIzMjNhZjg0MTM3MmEzZjdjNjAyYTQzNWM4MjhmYWE0MDNkMTc2YzZiMzdiNjA1YiJ9fX0=";

    private ItemBuilder builder;

    public ItemModifyMenu build(ItemStack it, Function<ItemStack, ItemStack> itemStackFunction) {
        builder = ItemBuilder.copy(it);
        return (ItemModifyMenu) title(p -> Lang.EDITOR_EDIT_ITEM1)
                //.background()
                .parentButton(0, 4)

                // color format description
                .button(8, 1, new Button.Builder()
                        .icon(p -> ItemBuilder.copy(Material.PAPER).name("&c\u2191 &7Item raw/color").build())
                )
                // count description
                //.button(0, 1, new Button.Builder()
                //        .icon(p -> ItemBuilder.copy(Material.PAPER).name("&c\u2191 &7Item min/max").build())
                //)

                // color unformatted item
                .button(7, 0, new Button.Builder()
                        .icon(p -> {
                            String lore = ColorUtil.invertRendered(builder.getLoreString());
                            return builder.copy().name(builder.getName(), ColorUtil.INVERT_RENDERED, "" + ChatColor.GRAY)
                                // GRAY will only be applied to the first line
                                // How to fix this
                                .lore(lore != null ? ChatColor.GRAY + String.join("\n" + ChatColor.GRAY, lore.split("\n")) : null, ColorUtil.AS_IS)
                                .build();
                        })
                )
                // color formatted item
                .button(8, 0, new Button.Builder()
                        .icon(p -> builder.copy().placeholders(p).renderAll().build())
                )



                // material search
                .childButton(2, 3, p -> ItemBuilder.copy(Material.COMPASS).name("&8Set material").lore("&7Search...").build(), new TextMenu.TBuilder()
                        .title(p -> "Material search")
                        .leftRaw(p -> builder.getModernMaterial().toLowerCase())
                        .onClose((player) -> Result.parent())
                        .onComplete(((player, s, tBuilder) -> {
                            try {
                                //builder.material(builder.apply(builder));
                                // how to set material for legacy items
                                // set damage value
                                builder = ItemBuilder.copy(itemStackFunction.apply(builder.material(s).build()));
                                return Result.parent();
                            } catch (Exception e) {
                                return Result.text("Does not exist");
                            }
                        }))
                )

                // edit Name
                .childButton(3, 1, p -> ItemBuilder.copy(Material.NAME_TAG).name(Lang.EDITOR_NAME).lore(Lang.EDITOR_LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.EDITOR_NAME)
                        .leftRaw(p -> builder.getNameOrLocaleName())
                        .right(p -> Lang.EDITOR_FORMATTING, p -> Editor.getColorDem(), ColorUtil.AS_IS)
                        .onClose((player) -> Result.parent())
                        .onComplete((player, s, b) -> {
                            if (s.isEmpty()) {
                                builder.removeName();
                            } else
                                builder.name(s, ColorUtil.RENDER_MARKERS);

                            builder = ItemBuilder.copy(itemStackFunction.apply(builder.build()));

                            return Result.parent();
                        }))

                // edit Lore                                                                                // terrible name
                .childButton(5, 1, p -> ItemBuilder.from("WRITABLE_BOOK").hideFlags(ItemFlag.HIDE_POTION_EFFECTS).name(Lang.LORE).lore(Lang.EDITOR_LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.LORE)
                        .leftRaw(p -> Util.def(builder.getLoreString(), Editor.LOREM_IPSUM).replace("\n", "\\n"))
                        .right(p -> Lang.EDITOR_FORMATTING, p -> Editor.getColorDem(), ColorUtil.AS_IS)
                        .onClose((player) -> Result.parent())
                        .onComplete((player, s, b) -> {
                            if (s.isEmpty()) {
                                builder.removeLore();
                            } else
                                builder.lore(s.replace("\\n", "\n"), ColorUtil.RENDER_MARKERS);

                            builder = ItemBuilder.copy(itemStackFunction.apply(builder.build()));

                            return Result.parent();
                        }))
                // Edit CustomModelData
                .childButton(4, 3, p -> ItemBuilder.fromSkull(BASE64_CUSTOM_MODEL_DATA).name(Lang.EDITOR_ITEM_MODEL).lore(Lang.EDITOR_LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.EDITOR_ITEM_MODEL)
                        .leftRaw(p -> {
                            ItemMeta meta = builder.getMeta();
                            if (meta != null && meta.hasCustomModelData())
                                return "" + meta.getCustomModelData();
                            return Editor.LOREM_IPSUM;
                        })
                        .right(p -> "&7" + Lang.EDITOR_ERROR_MODEL)
                        .onClose((player) -> Result.parent())
                        .onComplete((p, s, b) -> {
                            if (s.isEmpty())
                                return null;

                            int i;
                            try {
                                i = Integer.parseInt(s);
                            } catch (Exception e00) {
                                return Result.text(Lang.COMMAND_ERROR_INPUT);
                            }
                            builder.model(i);

                            builder = ItemBuilder.copy(itemStackFunction.apply(builder.build()));

                            return Result.parent();
                        }), Version.AT_LEAST_v1_16.a())


                // item swap
                .button(6, 3, new Button.Builder()
                        // if no custom name, don't even name
                        // just skip? or make an override to do nothing when null
                        //.icon(p -> builder.copy().name(builder.getName(), ColorUtil.RENDER_ALL, "" + ChatColor.GRAY).build())
                        .icon(p -> ItemBuilder.from("PLAYER_HEAD").name("&c&l[Item here]").build())
                        .lmb(interact -> {
                            if (interact.heldItem == null) {
                                return Result.message(Lang.EDITOR_ERROR_SWAP);
                            }
                            builder = ItemBuilder.copy(itemStackFunction.apply(interact.heldItem));

                            // Findings:
                            //  Item will be swapped out with the clicked item
                            //  Will result in menu elements being able to be pulled out
                            return Result.grab().andThen(Result.refresh());
                        }))

                ;

    }

}
