package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.EnumResult;
import com.crazicrafter1.gapi.SimpleMenu;
import com.crazicrafter1.gapi.TextMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

import static com.crazicrafter1.lootcrates.Editor.LORE_LMB_EDIT;

public class ItemMutateMenuBuilder extends SimpleMenu.SBuilder {

    public ItemMutateMenuBuilder() {
        super(5);
    }

    public static final String BASE64_CUSTOM_MODEL_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQ2NDg3NGRmNDUyYzFkNzE3ZWRkZDBmYjNiODQ4MjAyYWQxNTU3MTI0NWFmNmZhZGUyZWNmNTE0ZjNjODBiYiJ9fX0=";

    private ItemBuilder builder;

    public ItemMutateMenuBuilder build(ItemStack it, Consumer<ItemStack> itemStackConsumer) {
        builder = new ItemBuilder(it);
        return (ItemMutateMenuBuilder) title("item edit", true)
                .background()
                .parentButton(4, 4)
                .button(2, 1, Editor.IN_OUTLINE)
                .button(3, 2, Editor.IN_OUTLINE)
                .button(2, 3, Editor.IN_OUTLINE)
                .button(1, 2, Editor.IN_OUTLINE)
                .onClose(player -> {
                    if (builder != null)
                        itemStackConsumer.accept(builder.toItem());

                    return EnumResult.OK;
                })
                // Edit ItemStack
                .button(2, 2, new Button.Builder()
                        .icon(() -> builder.toItem())
                        .lmb(interact -> {
                            builder = new ItemBuilder(interact.heldItem.clone());
                            //if (interact.heldItem != null)
                                //lootSet.itemStack = interact.heldItem;
                                //itemStackConsumer.accept(interact.heldItem.clone());

                            return EnumResult.GRAB_ITEM;
                        }))
                // Edit Name
                .childButton(6, 1, () -> new ItemBuilder(Material.NAME_TAG).name("&eName").lore(LORE_LMB_EDIT).toItem(), new TextMenu.TBuilder()
                        .title("name", true)
                        .left(() -> Util.flattenedName(builder.toItem()))
                        //.text(Util.toAlternateColorCodes('&', lootItem.itemStack.getItemMeta().getDisplayName()))
                        //.leftInput(SAMPLE_LEFT)
                        .rightF(() -> "&8Input the name of the item", () -> Editor.COLOR_PREFIX)
                        //.rightInput(SAMPLE_RIGHT)
                        .onClose(player -> EnumResult.BACK)
                        .onComplete((player, s) -> {
                            if (s.isEmpty()) {
                                builder.resetName();
                            } else
                                builder.name(s);
                            return EnumResult.BACK;
                        }))
                // Edit Lore
                .childButton(6, 3, () -> new ItemBuilder(Material.MAP).name("&7Lore").lore(LORE_LMB_EDIT).toItem(), new TextMenu.TBuilder()
                        .title("lore", true)
                        .left(() -> Util.flattenedLore(builder.toItem(), "my custom lore"))
                        .rightF(() -> "&8Input the lore of the item", () -> Editor.COLOR_PREFIX + "&7\n'\\n': &fnewline")
                        .onClose(player -> EnumResult.BACK)
                        .onComplete((player, s) -> {
                            if (s.isEmpty()) {
                                builder.resetLore();
                            } else
                                builder.lore(s.replace("\\n", "\n"));
                            return EnumResult.BACK;
                        }))
                // Edit CustomModelData
                .childButton(6, 2, () -> new ItemBuilder(Material.PLAYER_HEAD).skull(BASE64_CUSTOM_MODEL_DATA).name("&8CustomModelData").lore(LORE_LMB_EDIT).toItem(), new TextMenu.TBuilder()
                        .title("model", true)
                        .left(() -> {
                            ItemMeta meta = builder.toItem().getItemMeta();
                            if (meta != null && meta.hasCustomModelData())
                                return "" + meta.getCustomModelData();
                            return "none";
                        })
                        .rightF(() -> "&7Input an integer")
                        .onClose(player -> EnumResult.BACK)
                        .onComplete((player, s) -> {
                            if (s.isEmpty())
                                return EnumResult.OK;

                            int i;
                            try {
                                i = Integer.parseInt(s);
                            } catch (Exception e00) {
                                return EnumResult.TEXT("Invalid");
                            }
                            builder.customModelData(i);
                            return EnumResult.BACK;
                        }));

    }

}
