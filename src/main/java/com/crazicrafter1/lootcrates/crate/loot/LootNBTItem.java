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

public class LootNBTItem extends LootItem {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.IRON_NUGGET).name("&bAdd NBT item...").lore("&7Use this to save all nbt tags").build();

    /**
     * Default ctor
     */
    public LootNBTItem() {
        this.item = ItemBuilder.copyOf(Material.STONE);
    }

    public LootNBTItem(Map<String, Object> args) {
        super(args);

        int rev = Main.get().rev;
        if (rev == 2)
            this.item = ItemBuilder.mutable(NMSAPI.getNBT((String) args.get("nbt")).setNBT(this.item.build()));
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return super.ofRange(p, item.build());
    }

    @Nonnull
    @Override
    public ItemStack getMenuIcon() {
        return item.copy().amount(min == max ? min : 1).build();
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("nbt", NMSAPI.getNBT(item.build()).serialize());
        return result;
    }

    @Nonnull
    @Override
    public ItemModifyMenu getMenuBuilder() {
        return (ItemModifyMenu) rangeButtons(new ItemModifyMenu()
                        .build(item.build(), input -> (this.item = ItemBuilder.mutable(input)).build()),
                item.build(), 3, 0, 5, 0);
    }
}
