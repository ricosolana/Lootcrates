package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.RandomUtil;
import com.crazicrafter1.crutils.ui.Button;
import com.crazicrafter1.lootcrates.*;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map;

public class LootItem extends AbstractLootItem {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copy(Material.GOLD_NUGGET).name("&6Add item...").build();

    public ItemStack itemStack;

    // TODO clone or not?
    public LootItem(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    /**
     * Editor template LootItem ctor
     */
    public LootItem() {
        //noinspection ConstantConditions
        itemStack = ItemBuilder.copy(RandomUtil.getRandomOf(Material.DIAMOND_PICKAXE, Material.GOLDEN_SWORD, Material.IRON_AXE)).build();
    }

    public LootItem(Map<String, Object> args) {
        super(args);

        // TODO eventually remove older revisions
        int rev = LCMain.get().rev;
        if (rev < 2)
            this.itemStack = (ItemStack) args.get("itemStack");
        else if (rev < 6)
            this.itemStack = ((ItemBuilder) args.get("item")).build();
        else
            this.itemStack = (ItemStack) args.get("item");

        if (itemStack == null) {
            LCMain.get().notifier.severe("A LootItem is null in config");
        }
    }

    // todo make protected after NBTItem removed
    public LootItem(LootItem other) {
        super(other);
        this.itemStack = other.itemStack.clone();
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return super.ofRange(p, itemStack);
    }

    @Nonnull
    @Override
    public ItemStack getMenuIcon() {
        // set count if min==max
        return ItemBuilder.copy(itemStack).amount(min == max ? min : 1).build();
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("item", itemStack);
        return result;
    }

    @Nonnull
    @Override
    public ItemModifyMenu getMenuBuilder() {
        return (ItemModifyMenu) rangeButtons(((ItemModifyMenu) new ItemModifyMenu()
                        .button(0, 1, new Button.Builder()
                                .icon(p -> ItemBuilder.copy(Material.PAPER).name("&c\u2191 &7Item min/max").build())
                        ))

                .build(itemStack, input -> this.itemStack = input),
                itemStack, 0, 0, 1, 0);
    }

    @Nonnull
    @Override
    public LootItem copy() {
        return new LootItem(this);
    }
}
