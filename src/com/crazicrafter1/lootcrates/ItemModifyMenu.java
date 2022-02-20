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

public class ItemModifyMenu extends SimpleMenu.SBuilder {

    public ItemModifyMenu() {
        super(5);
    }

    public static final String BASE64_CUSTOM_MODEL_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQ2NDg3NGRmNDUyYzFkNzE3ZWRkZDBmYjNiODQ4MjAyYWQxNTU3MTI0NWFmNmZhZGUyZWNmNTE0ZjNjODBiYiJ9fX0=";

    private ItemBuilder builder;

    public ItemModifyMenu build(ItemStack it, Consumer<ItemStack> itemStackConsumer) {
        builder = ItemBuilder.copyOf(it);
        return (ItemModifyMenu) title("item edit")
                .background()
                .parentButton(4, 4)
                .button(2, 1, IN_OUTLINE)
                .button(3, 2, IN_OUTLINE)
                .button(2, 3, IN_OUTLINE)
                .button(1, 2, IN_OUTLINE)
                // Edit ItemStack
                .button(2, 2, new Button.Builder()
                        .icon(() -> builder.build())
                        .lmb((interact) -> {
                            if (interact.heldItem == null) {
                                return Result.MESSAGE("Must swap with an item");
                            }
                            builder = ItemBuilder.copyOf(interact.heldItem);

                            itemStackConsumer.accept(builder.build());

                            return Result.GRAB();
                        }))
                // Edit Name
                .childButton(6, 1, () -> ItemBuilder.copyOf(Material.NAME_TAG).name("&eName").lore(LORE_LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title("name")
                        .leftRaw(() -> builder.getNameOrLocaleName(), null, ColorMode.REVERT)
                        .right(() -> "&8Input custom item name", () -> COLOR_PREFIX)
                        .onClose((player) -> Result.PARENT())
                        .onComplete((player, s) -> {
                            if (s.isEmpty()) {
                                builder.resetName();
                            } else
                                builder.name(s);

                            itemStackConsumer.accept(builder.build());

                            return Result.PARENT();
                        }))
                // Edit Lore
                .childButton(6, 3, () -> ItemBuilder.copyOf(Material.MAP).name("&7Lore").lore(LORE_LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title("lore")
                        .leftRaw(() -> Util.strDef(builder.getLoreString(), LOREM_IPSUM), null, ColorMode.REVERT)
                        .right(() -> "&8Input custom item lore", () -> COLOR_PREFIX + "&7\n'\\n': &fnewline")
                        .onClose((player) -> Result.PARENT())
                        .onComplete((player, s) -> {
                            if (s.isEmpty()) {
                                builder.resetLore();
                            } else
                                builder.lore(s.replace("\\n", "\n"));

                            itemStackConsumer.accept(builder.build());

                            return Result.PARENT();
                        }))
                // Edit CustomModelData
                .childButton(6, 2, () -> ItemBuilder.of("PLAYER_HEAD").skull(BASE64_CUSTOM_MODEL_DATA).name("&8CustomModelData").lore(LORE_LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title("model")
                        .leftRaw(() -> {
                            ItemMeta meta = builder.getMeta();
                            if (meta != null && meta.hasCustomModelData())
                                return "" + meta.getCustomModelData();
                            return "none";
                        })
                        .right(() -> "&7Input an integer")
                        .onClose((player) -> Result.PARENT())
                        .onComplete((p, s) -> {
                            if (s.isEmpty())
                                return null;

                            int i;
                            try {
                                i = Integer.parseInt(s);
                            } catch (Exception e00) {
                                return Result.TEXT(L(p, "Invalid"));
                            }
                            builder.model(i);

                            itemStackConsumer.accept(builder.build());

                            return Result.PARENT();
                        }), Version.AT_LEAST_v1_16.a());

    }

}
