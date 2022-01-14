package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.lootcrates.Editor;
import com.crazicrafter1.lootcrates.ItemMutateMenuBuilder;
import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
    public ItemStack getIcon(Player p) {
        ItemStack i = super.ofRange(p, itemStack);

        Main.get().info("getIcon itemStack: " + itemStack);
        Main.get().info("getIcon i: " + i);

        return i;
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
                            int change = interact.shift ? 5 : 1;
                            min = Util.clamp(min - change, 1, min);
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            min = Util.clamp(min + change, 1, max);
                            return Result.REFRESH();
                        })
                        .icon(() -> new ItemBuilder(ReflectionUtil.isAtLeastVersion("1_17") ? Material.MEDIUM_AMETHYST_BUD : Material.RED_DYE).name("&8&nMin").lore(Editor.LORE_LMB_NUM + "\n" + Editor.LORE_RMB_NUM + "\n" + Editor.LORE_SHIFT_NUM).count(min).toItem()))
                // Max
                .button(7, 2, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = Util.clamp(max - change, min, itemStack.getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = Util.clamp(max + change, min, itemStack.getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .icon(() -> new ItemBuilder(ReflectionUtil.isAtLeastVersion("1_17") ? Material.AMETHYST_CLUSTER : Material.GREEN_DYE).name("&8&nMax").lore(Editor.LORE_LMB_NUM + "\n" + Editor.LORE_RMB_NUM + "\n" + Editor.LORE_SHIFT_NUM).count(max).toItem()));
    }
}
