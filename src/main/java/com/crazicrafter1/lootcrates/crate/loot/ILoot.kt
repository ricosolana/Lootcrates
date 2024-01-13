package com.crazicrafter1.lootcrates.crate.loot

import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.lootcrates.crate.CrateInstance
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Must create a static method in class to get the Editor-representative item
 */
interface ILoot : ConfigurationSerializable {
    /**
     * Return the visible icon as an [ItemStack] that the player
     * will see as a representation of loot in a crate when showed
     * @return ItemStack
     */

    fun getRenderIcon(p: Player): ItemStack

    /**
     * The actual loot action to do on click
     * @param activeCrate the reference crate
     * @return whether the player should keep the clicked [ItemStack]
     */
    // TODO return something more descriptive than a bool
    fun execute(activeCrate: CrateInstance): Boolean

    val menuBuilder: AbstractMenu.Builder

    val menuIcon: ItemStack

    val menuDesc: String

    fun copy(): ILoot
}
