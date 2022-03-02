package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractLootItem implements ILoot {

    public int min;
    public int max;

    public AbstractLootItem() {
        min = 1;
        max = 1;
    }

    public AbstractLootItem(Map<String, Object> args) {
        this.min = (int) args.get("min");
        this.max = (int) args.get("max");

        if (min > max)
            throw new IllegalArgumentException("failed to assert: min <= max");
    }

    @Nonnull
    public abstract ItemStack getRenderIcon(@Nonnull Player p);

    @Override
    public final boolean execute(@Nonnull ActiveCrate activeCrate) {
        return true;
    }

    @Nonnull
    public String getMenuDesc(@Nonnull Player p) {
        StringBuilder sb = new StringBuilder();
        if (min == max)
            sb.append(String.format(Lang.ITEM_COUNT, min));
        else sb.append(String.format(Lang.ITEM_COUNT, min, max));

        return sb.toString();
    }

    @Nonnull
    protected ItemStack ofRange(@Nonnull Player p, @Nonnull ItemStack itemStack) {
        return ItemBuilder.copyOf(itemStack).amount(Util.randomRange(min, max)).placeholders(p).renderAll().build();
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("min", min);
        result.put("max", max);

        return result;
    }
}
