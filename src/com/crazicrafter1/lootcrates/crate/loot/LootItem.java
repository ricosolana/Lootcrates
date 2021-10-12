package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.Main;
import org.bukkit.ChatColor;
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
        Button.Builder inOutline = new Button.Builder().icon(() -> new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&7Set to").toItem());
        return new SimpleMenu.SBuilder(5)
                .title(ChatColor.DARK_GRAY + "LootItem editor")
                .background()
                .parentButton(4, 4)
                .button(2, 1, inOutline)
                .button(3, 2, inOutline)
                .button(2, 3, inOutline)
                .button(1, 2, inOutline)
                // Edit ItemStack
                .button(2, 2, new Button.Builder()
                        .icon(() -> itemStack)
                        .lmb(interact -> {
                            if (interact.heldItem != null)
                                itemStack = interact.heldItem;
                            return EnumResult.GRAB_ITEM;
                        }))
                // Edit Name
                .childButton(6, 1, () -> new ItemBuilder(Material.NAME_TAG).name("&eName").lore("&8LMB: &aEdit").toItem(), new TextMenu.TBuilder()
                        .title("&8Edit name")
                        //.text(Util.toAlternateColorCodes('&', lootItem.itemStack.getItemMeta().getDisplayName()))
                        .leftInput(new Button.Builder().icon(() -> new ItemBuilder(Material.IRON_SWORD).mergeLexicals(itemStack).toItem()))
                        .rightInput(new Button.Builder().icon(() -> new ItemBuilder(Material.IRON_SWORD).name("&8Use '&' for colors").toItem()))
                        .onClose(player -> EnumResult.BACK)
                        .onComplete((player, s) -> {
                            if (s.isEmpty())
                                return EnumResult.OK;
                            itemStack = new ItemBuilder(itemStack).name(s).toItem();
                            return EnumResult.BACK;
                        }))
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
                        .icon(() -> new ItemBuilder(Material.MEDIUM_AMETHYST_BUD).name("&8&nMin").lore("&7LMB: &c-\n&7RMB: &a+\n&fShift: &6x5").count(min).toItem()))
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
                        .icon(() -> new ItemBuilder(Material.AMETHYST_CLUSTER).name("&8&nMax").lore("&7LMB: &c-\n&7RMB: &a+\n&fShift: &6x5").count(max).toItem()))
                // Edit Lore
                .childButton(6, 3, () -> new ItemBuilder(Material.MAP).name("&7Lore").lore("&8LMB: &aEdit").toItem(), new TextMenu.TBuilder()
                        .title("&8Item lore")
                        .leftInput(new Button.Builder().icon(() -> new ItemBuilder(Material.IRON_SWORD).mergeLexicals(itemStack).toItem()))
                        .rightInput(new Button.Builder().icon(() -> new ItemBuilder(Material.IRON_SWORD).name("&8Use '&7&&8' for colors").lore("&8Use '&7\\n&8' for multiline").toItem()))
                        //.leftInput(new Button.Builder().icon(() -> itemStack))
                        //.rightInput(new Button.Builder().icon(() -> new ItemBuilder(itemStack.getType()).name("&8Use '&' for colors").lore("&7Ignore the &cX &7>").toItem()))
                        .onClose(player -> EnumResult.BACK)
                        .onComplete((player, s) -> {
                            if (s.isEmpty())
                                return EnumResult.OK;
                            itemStack = new ItemBuilder(itemStack).lore(s.replace("\\n", "\n")).toItem();
                            return EnumResult.BACK;
                        }))
                .validate();
    }
}
