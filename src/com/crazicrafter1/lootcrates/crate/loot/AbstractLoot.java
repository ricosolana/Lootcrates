package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

/**
 * TODO
 *  Abstract loot class is more or less abstracted config data
 *
 *  ActiveLoot is a loot that has calculated values upon being revealed
 *      - what to show on reveal
 *      - what to do on click
 */
public abstract class AbstractLoot implements ConfigurationSerializable {

    public AbstractLoot() {
        //Main.get().info("AbstractLoot constructed: " + getClass().getSimpleName());
    }

    public abstract ItemStack getIcon();

    public abstract void execute(ActiveCrate activeCrate, boolean closed, boolean[] giveItem);

    @Override
    public String toString() {
        return "icon: &7" + getIcon() + "\n";
    }

    public abstract AbstractMenu.Builder getMenuBuilder();
}
