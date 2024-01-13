package com.crazicrafter1.lootcrates.crate.loot

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.crutils.ui.ListMenu.LBuilder
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.crutils.ui.TextMenu.TBuilder
import com.crazicrafter1.lootcrates.*
import me.zombie_striker.customitemmanager.CustomBaseObject
import me.zombie_striker.qg.api.QualityArmory
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LootItemQA : AbstractLootItem {
    var name: String?

    /**
     * Editor template LootItemQA ctor
     */
    constructor() {
        // just the first loaded item
        name = QualityArmory.getCustomItems().next().name
    }

    protected constructor(other: LootItemQA) : super(other) {
        name = other.name
    }

    constructor(args: Map<String?, Any?>) : super(args) {
        name = args["name"] as String?
    }

    override fun getRenderIcon(p: Player): ItemStack {
        return ofRange(p, QualityArmory.getCustomItemAsItemStack(name))
    }

    override val menuIcon: ItemStack
        get() = QualityArmory.getCustomItemAsItemStack(name)
    override val menuDesc: String
        get() = """
            &8Quality armory: &f$name
            ${super.getMenuDesc()}
            """.trimIndent()

    @Nonnull
    override fun serialize(): MutableMap<String, Any> {
        val result = super.serialize()
        result["name"] = name
        return result
    }

    @get:Nonnull
    override val menuBuilder: AbstractMenu.Builder?
        get() = rangeButtons(LBuilder()
                .parentButton(4, 5) //.childButton(2, 5, () -> new ItemBuilder(Material.COMPASS).name("&eSearch..."), new )
                .button(3, 5, Button.Builder().icon { p: Player? -> menuIcon })
                .childButton(5, 5, { p: Player? -> ItemBuilder.copy(Material.COMPASS).name(Lang.ASSIGN_EXACT).build() }, TBuilder()
                        .title { p: Player? -> Lang.ASSIGN_EXACT }
                        .leftRaw { p: Player? -> name }
                        .right { p: Player? -> Lang.EDITOR_ITEM_SET }
                        .onClose { player: Player? -> Result.parent() }
                        .onComplete { p: Player?, s: String?, b: TBuilder? ->
                            val customBaseObject = QualityArmory.getCustomItemByName(s)
                            if (customBaseObject != null) {
                                name = s
                                return@onComplete Result.parent()
                            }
                            Result.text(Lang.COMMAND_ERROR_INPUT)
                        }
                )
                .addAll { self: LBuilder?, p00: Player? ->
                    val result = ArrayList<Button>()
                    QualityArmory.getCustomItems().forEachRemaining { customBaseObject: CustomBaseObject ->
                        if (customBaseObject.name == name) {
                            result.add(Button.Builder()
                                    .icon { p: Player? -> menuIcon }
                                    .get())
                        } else {
                            result.add(Button.Builder()
                                    .icon { p: Player? -> QualityArmory.getCustomItemAsItemStack(customBaseObject.name) }
                                    .lmb { interact: Button.Event? ->
                                        // change
                                        name = customBaseObject.name
                                        Result.parent()
                                    }.get()
                            )
                        }
                    }
                    result
                }, menuIcon, 1, 5, 2, 5)

    override fun copy(): LootItemQA {
        return LootItemQA()
    }

    companion object {
        val EDITOR_ICON = ItemBuilder.copy(Material.CROSSBOW).name("&8Add QualityArmory item...").build()
    }
}
