package com.crazicrafter1.lootcrates.crate.loot

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.lootcrates.ItemModifyMenu
import com.crazicrafter1.lootcrates.LCMain
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Function

class LootItem : AbstractLootItem {
    var itemStack: ItemStack;

    // TODO clone or not?
    constructor(itemStack: ItemStack) {
        this.itemStack = itemStack.clone()
    }

    /**
     * Editor template LootItem ctor
     */
    constructor() {
        itemStack = ItemBuilder.copy(RandomUtil.getRandomOf(Material.DIAMOND_PICKAXE, Material.GOLDEN_SWORD, Material.IRON_AXE)).build()
    }

    constructor(args: Map<String?, Any?>) : super(args) {

        // TODO eventually remove older revisions
        val rev: Int = LCMain.get()!!.rev
        if (rev < 2) itemStack = args["itemStack"] as ItemStack? else if (rev < 6) itemStack = (args["item"] as ItemBuilder?)!!.build() else itemStack = args["item"] as ItemStack?
        if (itemStack == null) {
            LCMain.get()!!.notifier!!.severe("A LootItem is null in config")
        }
    }

    // todo make protected after NBTItem removed
    constructor(other: LootItem) : super(other) {
        itemStack = other.itemStack!!.clone()
    }

    override fun getRenderIcon(p: Player): ItemStack {
        return super.ofRange(p, itemStack)
    }

    override val menuIcon: ItemStack
        get() =// set count if min==max
            ItemBuilder.copy(itemStack).amount(if (min == max) min else 1).build()

    override fun serialize(): MutableMap<String, Any> {
        val result = super.serialize()
        result["item"] = itemStack
        return result
    }

    override val menuBuilder: AbstractMenu.Builder
        get() = rangeButtons((ItemModifyMenu()
                .button(0, 1, Button.Builder()
                        .icon { p: Player? -> ItemBuilder.copy(Material.PAPER).name("&c\u2191 &7Item min/max").build() }
                ) as ItemModifyMenu)
                .build(itemStack, Function { input: ItemStack? -> itemStack = input }),
                itemStack!!, 0, 0, 1, 0) as ItemModifyMenu

    override fun copy(): LootItem {
        return LootItem(this)
    }

    companion object {
        val EDITOR_ICON = ItemBuilder.copy(Material.GOLD_NUGGET).name("&6Add item...").build()
    }
}
