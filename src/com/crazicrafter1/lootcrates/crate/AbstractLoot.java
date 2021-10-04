package com.crazicrafter1.lootcrates.crate;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * TODO
 *  Abstract loot class is more or less abstracted config data
 *
 *  ActiveLoot is a loot that has calculated values upon being revealed
 *      - what to show on reveal
 *      - what to do on click
 */
public abstract class AbstractLoot implements ConfigurationSerializable {

    public ItemStack icon;

    public AbstractLoot(ItemStack baseVisual) {

        if (baseVisual == null)
            throw new RuntimeException("Item must not be null!");

        this.icon = baseVisual;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public abstract void execute(ActiveCrate activeCrate, boolean closed, boolean[] giveItem);

    @Override
    public String toString() {
        return "icon: &7" + getIcon() + "\n";
    }

    @Override
    public Map<String, Object> serialize() {
        throw new RuntimeException("I must be overridden!");
    }


}
