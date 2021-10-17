package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.EnumResult;
import com.crazicrafter1.lootcrates.Editor;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.ItemMutateMenuBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class LootItem extends AbstractLootItem {

    public ItemStack itemStack;

    /**
     * Default ctor
     */
    public LootItem() {
        this(new ItemStack(Material.STONE));
    }

    public LootItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public LootItem(Map<String, Object> args) {
        super(args);
        this.itemStack = (ItemStack) args.get("itemStack");
        if (itemStack == null) {
            Main.get().error(args.toString());
            throw new NullPointerException("Item must not be null");
        }
    }

    @Override
    public ItemStack getIcon() {
        return super.ofRange(itemStack);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("itemStack", itemStack);
        return result;
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        //Button.Builder inOutline = new Button.Builder().icon(() -> new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&7Set to").toItem());
        return new ItemMutateMenuBuilder()
                .build(itemStack, input -> this.itemStack = input)
                .title("LootItem")
                // Min
                .button(5, 2, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.isShift() ? 5 : 1;
                            min = Util.clamp(min - change, 1, min);
                            return EnumResult.REFRESH;
                        })
                        .rmb(interact -> {
                            int change = interact.isShift() ? 5 : 1;
                            min = Util.clamp(min + change, 1, max);
                            return EnumResult.REFRESH;
                        })
                        .icon(() -> new ItemBuilder(Material.MEDIUM_AMETHYST_BUD).name("&8&nMin").lore(Editor.LORE_LMB_NUM + "\n" + Editor.LORE_RMB_NUM + "\n" + Editor.LORE_SHIFT_NUM).count(min).toItem()))
                // Max
                .button(7, 2, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.isShift() ? 5 : 1;
                            max = Util.clamp(max - change, min, itemStack.getMaxStackSize());
                            return EnumResult.REFRESH;
                        })
                        .rmb(interact -> {
                            int change = interact.isShift() ? 5 : 1;
                            max = Util.clamp(max + change, min, itemStack.getMaxStackSize());
                            return EnumResult.REFRESH;
                        })
                        .icon(() -> new ItemBuilder(Material.AMETHYST_CLUSTER).name("&8&nMax").lore(Editor.LORE_LMB_NUM + "\n" + Editor.LORE_RMB_NUM + "\n" + Editor.LORE_SHIFT_NUM).count(max).toItem()));
    }
}
