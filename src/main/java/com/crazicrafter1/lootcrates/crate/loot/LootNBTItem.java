package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.nmsapi.NMSAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map;

// todo migrate post to LootItem
@Deprecated
public class LootNBTItem extends LootItem {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copy(Material.IRON_NUGGET).name("&bAdd NBT item...").lore("&7Use this to save all nbt tags").build();

    /**
     * Default ctor
     */
    public LootNBTItem() {
        super();
    }

    public LootNBTItem(Map<String, Object> args) {
        super(args);

        this.item = NMSAPI.getNBT((String) args.get("nbt")).setNBT(this.item);
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return super.ofRange(p, item);
    }

    @Nonnull
    @Override
    public ItemStack getMenuIcon() {
        return ItemBuilder.copy(item).amount(min == max ? min : 1).build();
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        throw new UnsupportedOperationException("Do not use!");
    }

    @Nonnull
    @Override
    public ItemModifyMenu getMenuBuilder() {
        return (ItemModifyMenu) rangeButtons(new ItemModifyMenu()
                        .build(item, input -> this.item = input),
                item, 3, 0, 5, 0);
    }
}
