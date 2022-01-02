package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * TODO
 *  Abstract loot class is more or less abstracted config data
 *
 *  ActiveLoot is a loot that has calculated values upon being revealed
 *      - what to show on reveal
 *      - what to do on click
 */
public interface ILoot extends ConfigurationSerializable {

    /**
     * Return the visible icon as an {@link ItemStack} that the player
     * will see as a representation of loot in a crate when showed
     * @return ItemStack
     */
    ItemStack getIcon(Player p);

    /**
     * The actual loot action to do on click
     * @param activeCrate the reference crate
     * @return whether the player should keep the clicked {@link ItemStack}
     */
    boolean execute(ActiveCrate activeCrate);

    /**
     * The utility menu to show in the editor to modify an ILoot instance
     * @return
     */
    AbstractMenu.Builder getMenuBuilder();
}
