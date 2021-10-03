package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.Bool;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.crate.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class LootOrdinateItem extends AbstractLoot {

    public int min;
    public int max;

    /*
     * Default constructor
     */
    public LootOrdinateItem() {
        super(new ItemStack(Material.AIR));
        min = 1;
        max = 1;
    }

    public LootOrdinateItem(Map<String, Object> args) {
        super((ItemStack) args.get("item"));
        min = (int)args.get("min");
        max = (int)args.get("max");
    }

    public LootOrdinateItem(ItemStack itemStack, int min, int max) {
        super(itemStack);
        this.min = min;
        this.max = max;
    }

    public LootOrdinateItem(ItemStack itemStack, Map<String, Object> args) {
        super(itemStack);
        min = (int)args.get("min");
        max = (int)args.get("max");
    }

    @Override
    public final void execute(ActiveCrate activeCrate, boolean closed, Bool giveItem) {
        // if inventory was closed, items must be automatically given
        if (closed) {
            Util.giveItemToPlayer(activeCrate.getPlayer(), getIcon());
        } else
            giveItem.value = true;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(super.getIcon())
                .count(Util.randomRange(min, max)).toItem();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (min == max)
            sb.append("count: ").append(min);
        else sb.append("range: [").append(min).append(", ").append(max).append("]");

        return sb.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("item", super.getIcon());

        result.put("min", min);
        result.put("max", max);

        // then other impls...


        return result;
    }
}
