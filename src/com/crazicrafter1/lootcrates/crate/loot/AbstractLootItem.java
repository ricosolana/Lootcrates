package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractLootItem extends AbstractLoot {

    public int min;
    public int max;

    public AbstractLootItem() {
        min = 1;
        max = 1;
    }

    public AbstractLootItem(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public AbstractLootItem(Map<String, Object> args) {
        this((int)args.get("min"), (int)args.get("max"));

        if (min > max)
            throw new IndexOutOfBoundsException("failed to assert: min <= max");
    }

    protected ItemStack ofRange(ItemStack itemStack) {
        return new ItemBuilder(itemStack).count(Util.randomRange(min, max)).toItem();
    }

    @Override
    public final void execute(ActiveCrate activeCrate, boolean closed, boolean[] giveItem) {
        // if inventory was closed, items must be automatically given
        if (closed) {
            Util.giveItemToPlayer(activeCrate.getPlayer(), getIcon());
        } else
            giveItem[0] = true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (min == max)
            sb.append("&8count: &7").append(min);
        else sb.append("&8range: &7[").append(min).append(", ").append(max).append("]");

        return sb.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("min", min);
        result.put("max", max);

        return result;
    }
}
