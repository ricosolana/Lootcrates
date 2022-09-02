package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
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

        this.itemStack = NMSAPI.getNBT((String) args.get("nbt")).setNBT(this.itemStack);
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        throw new RuntimeException("Stub");
    }

    @Nonnull
    @Override
    public ItemStack getMenuIcon() {
        throw new RuntimeException("Stub");
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        throw new RuntimeException("Stub");
    }

    @Nonnull
    @Override
    public ItemModifyMenu getMenuBuilder() {
        throw new RuntimeException("Stub");
    }
}
