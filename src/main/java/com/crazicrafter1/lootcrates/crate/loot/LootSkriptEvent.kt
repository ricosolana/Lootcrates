package com.crazicrafter1.lootcrates.crate.loot

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.crutils.ui.TextMenu.TBuilder
import com.crazicrafter1.lootcrates.*
import com.crazicrafter1.lootcrates.crate.CrateInstance
import com.crazicrafter1.lootcrates.sk.SkriptLootEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Function

class LootSkriptEvent : ILoot {
    var tag: String?
    var item: ItemStack? = null

    constructor() {
        tag = "my_skript_tag"
        item = ItemStack(Material.JUKEBOX)
    }

    protected constructor(other: LootSkriptEvent) {
        tag = other.tag
        item = other.item
    }

    constructor(result: Map<String?, Any?>) {
        // idk
        tag = result["tag"] as String?

        // TODO eventually remove older revisions
        val rev: Int = LCMain.Companion.get()!!.rev
        item = if (rev < 2) result["itemStack"] as ItemStack? else if (rev < 6) (result["item"] as ItemBuilder?)!!.build() else result["item"] as ItemStack?
    }

    @Nonnull
    override fun getRenderIcon(@Nonnull p: Player): ItemStack {
        return ItemBuilder.copy(item).placeholders(p).renderAll().build()
    }

    override fun execute(activeCrate: CrateInstance): Boolean {
        Bukkit.getServer().pluginManager.callEvent(SkriptLootEvent(tag, activeCrate.player))
        return false
    }

    override val menuIcon: ItemStack
        get() = item!!.clone()
    override val menuDesc: String
        get() = "&7tag: &f$tag"

    @get:Nonnull
    override val menuBuilder: AbstractMenu.Builder?
        get() = ItemModifyMenu()
                .build(item, Function { input: ItemStack? -> item = input })
                .childButton(1, 0, { p: Player? -> ItemBuilder.copy(Material.PAPER).name(Lang.EDITOR_LOOT_SKRIPT_TITLE).lore(Lang.EDITOR_LMB_EDIT).build() }, TBuilder()
                        .title { p: Player? -> Lang.EDITOR_LOOT_SKRIPT_TITLE }
                        .onClose { player: Player? -> Result.parent() }
                        .leftRaw { p: Player? -> tag }
                        .right { p: Player? -> Lang.EDITOR_LOOT_SKRIPT_INPUT }
                        .onComplete { p: Player?, s: String, b: TBuilder? ->
                            if (!s.isEmpty()) {
                                tag = s
                                return@onComplete Result.parent()
                            }
                            Result.text(Lang.COMMAND_ERROR_INPUT)
                        })

    override fun serialize(): Map<String, Any> {
        val result: MutableMap<String, Any> = LinkedHashMap()
        result["tag"] = tag!!
        result["item"] = item!!
        return result
    }

    override fun copy(): LootSkriptEvent {
        return LootSkriptEvent(this)
    }

    companion object {
        val EDITOR_ICON = ItemBuilder.copy(Material.MAP).name("&aAdd Skript tag...").build()
    }
}
