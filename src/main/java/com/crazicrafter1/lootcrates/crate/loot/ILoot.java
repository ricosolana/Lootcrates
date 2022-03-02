package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Must create a static method in class to get the Editor-representative item
 */
public interface ILoot extends ConfigurationSerializable {

    /**
     * Return the visible icon as an {@link ItemStack} that the player
     * will see as a representation of loot in a crate when showed
     * @return ItemStack
     */
    @Nonnull
    ItemStack getRenderIcon(@Nonnull Player p);

    /**
     * The actual loot action to do on click
     * @param activeCrate the reference crate
     * @return whether the player should keep the clicked {@link ItemStack}
     */
    boolean execute(@Nonnull ActiveCrate activeCrate);

    /**
     * The utility menu to show in the editor to modify an ILoot instance
     *  TODO Important: Must not assign a onClose(...) function to the menu
     *      This results in unexpected menu behaviour
     *      I have no idea how to fix this issue ATM.
     * @return {@link AbstractMenu.Builder} instance
     */
    @Nonnull
    AbstractMenu.Builder getMenuBuilder();

    /**
     * Return the visible icon as an {@link ItemStack} that the player
     * will see as a representation of loot in a menu when showed
     * @return ItemStack
     */
    @Nonnull
    ItemStack getMenuIcon(@Nonnull Player p);

    @Nonnull
    String getMenuDesc(@Nonnull Player p);
}
