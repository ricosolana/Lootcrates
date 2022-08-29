package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.RandomUtil;
import com.crazicrafter1.lootcrates.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;

public class LootItem extends AbstractLootItem {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copy(Material.GOLD_NUGGET).name("&6Add item...").build();

    public ItemStack itemStack;

    /**
     * Editor template LootItem ctor
     */
    public LootItem() {
        //noinspection ConstantConditions
        itemStack = ItemBuilder.copy(RandomUtil.getRandom(Arrays.asList(Material.DIAMOND_PICKAXE, Material.GOLDEN_SWORD, Material.IRON_AXE))).build();
    }

    public LootItem(Map<String, Object> args) {
        super(args);

        // TODO eventually remove older revisions
        int rev = Main.get().rev;
        if (rev < 2)
            this.itemStack = (ItemStack) args.get("itemStack");
        else if (rev < 6)
            this.itemStack = ((ItemBuilder) args.get("item")).build();
        else
            this.itemStack = (ItemStack) args.get("item");

        if (itemStack == null) {
            Main.get().notifier.severe("A LootItem is null in config");
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
        return (ItemModifyMenu) rangeButtons(new ItemModifyMenu()
                .build(itemStack, input -> this.itemStack = input),
                itemStack, 3, 0, 5, 0);
    }

    @NotNull
    @Override
    public LootItem copy() {
        return new LootItem(this);
    }
}
