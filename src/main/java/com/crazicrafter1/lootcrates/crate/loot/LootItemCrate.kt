package com.crazicrafter1.lootcrates.crate.loot

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.crutils.ui.ListMenu.LBuilder
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.lootcrates.*
import com.crazicrafter1.lootcrates.crate.CrateInstance
import com.crazicrafter1.lootcrates.crate.CrateSettings
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class LootItemCrate : ILoot {
    var id: String? = null

    /**
     * Editor template LootItemCrate ctor
     */
    constructor() {
        try {
            id = LCMain.Companion.get()!!.rewardSettings!!.crates!!.keys.iterator().next()
        } catch (e: NoSuchElementException) {
            LCMain.Companion.get()!!.notifier!!.severe(Lang.COMMAND_ERROR_CRATES)
        }
    }

    protected constructor(other: LootItemCrate) {
        id = other.id
    }

    constructor(args: Map<String?, Any?>) {
        id = args["crate"] as String?
    }

    override fun execute(activeCrate: CrateInstance): Boolean {
        return true
    }

    @Nonnull
    override fun getRenderIcon(@Nonnull p: Player): ItemStack {
        val crate: CrateSettings = LCMain.Companion.get()!!.rewardSettings!!.crates!!.get(id)
        return Objects.requireNonNull(crate,
                "Referred a crate by name (" + id + ") " +
                        "which doesn't have a definition in config").itemStack(p)
    }

    override val menuIcon: ItemStack
        get() = LCMain.Companion.get()!!.rewardSettings!!.crates!!.get(id)!!.item!!.clone()
    override val menuDesc: String
        get() = "&7Crate: &f$id"

    override fun serialize(): Map<String, Any> {
        val result: MutableMap<String, Any> = HashMap() // HashMap used and not LinkedHashMap since only 1 element is added
        result["crate"] = id!!
        return result
    }

    override val menuBuilder: AbstractMenu.Builder?
        get() = LBuilder()
                .parentButton(4, 5)
                .addAll { self: LBuilder?, p00: Player? ->
                    val result = ArrayList<Button>()
                    for ((_, crate) in LCMain.Companion.get()!!.rewardSettings!!.crates!!.entries) {

                        //ItemStack icon = ItemBuilder.copyOf(Material.LOOM).apply(crate.item).glow(crate.id.equals(id)).build();
                        result.add(Button.Builder()
                                .icon { p: Player? -> ItemBuilder.copy(crate!!.item).glow(crate!!.id == id).build() }
                                .lmb { interact: Button.Event? ->
                                    // select as active
                                    id = crate!!.id
                                    Result.refresh()
                                }
                                .get()
                        )
                    }
                    result
                }

    override fun copy(): LootItemCrate {
        return LootItemCrate(this)
    }

    companion object {
        val EDITOR_ICON = ItemBuilder.copy(Material.CHEST).name("&eAdd crate...").build()
    }
}
