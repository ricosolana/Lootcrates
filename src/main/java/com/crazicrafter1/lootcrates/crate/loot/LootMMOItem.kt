package com.crazicrafter1.lootcrates.crate.loot

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.crutils.ui.SimpleMenu.SBuilder
import com.crazicrafter1.crutils.ui.TextMenu.TBuilder
import com.crazicrafter1.lootcrates.*
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.player.PlayerData
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.regex.Pattern

class LootMMOItem : AbstractLootItem {
    var type: String?
    var name: String?
    var mode: Int
    var level = 0
    var tier: String? = null
    //special delimiter reader/writer for
    // 0: Level, Tier
    // 1: Random
    // 2: Scale with player
    /**
     * Editor template LootMMOItem ctor
     */
    // https://git.lumine.io/mythiccraft/mmoitems/-/wikis/Main%20API%20Features
    constructor() {
        //this("SWORD", "CUTLASS", 0, 1, "UNCOMMON");
        type = "SWORD"
        name = "CUTLASS"
        mode = 0
        level = 1
        tier = "UNCOMMON"
    }

    protected constructor(other: LootMMOItem) : super(other) {
        //this(other.type, other.name, other.mode, other.level, other.tier);
        type = other.type
        name = other.name
        mode = other.mode
        level = other.level
        tier = other.tier
    }

    //private LootMMOItem(String type, String name, int mode, int level, String tier) {
    //    this.type = type;
    //    this.name = name;
    //    this.mode = mode;
    //    this.level = level;
    //    this.tier = tier;
    //}
    constructor(args: Map<String?, Any?>) : super(args) {
        type = args["type"] as String?
        name = args["name"] as String?
        mode = args["mode"] as Int
        if (mode == 0) {
            level = args["level"] as Int
            tier = args["tier"] as String?
        }
    }

    @Nonnull
    override fun getRenderIcon(@Nonnull p: Player): ItemStack {
        return if (mode == 0) { // exact
            val itemTier = MMOItems.plugin.tiers.getOrThrow(tier)
            ofRange(p, MMOItems.plugin.getItem(
                    MMOItems.plugin.types[type], name, level, itemTier))
        } else if (mode == 1) { // random
            ofRange(p, MMOItems.plugin.getItem(
                    MMOItems.plugin.types[type], name))
        } else { // scale
            ofRange(p, MMOItems.plugin.getItem(
                    MMOItems.plugin.types[type], name, PlayerData.get(p.uniqueId)))
        }
    }

    override val menuIcon: ItemStack
        get() {
            val itemTier = MMOItems.plugin.tiers.getOrThrow(tier)
            return MMOItems.plugin.getItem(
                    MMOItems.plugin.types[type], name)!!
        }
    override val menuDesc: String
        get() = """
            &8MMOItem: &f$type:$name
            ${super.getMenuDesc()}
            """.trimIndent()

    override fun serialize(): MutableMap<String, Any> {
        val result = super.serialize()
        result["type"] = type
        result["name"] = name
        result["mode"] = mode
        if (mode == 0) {
            result["level"] = level
            result["tier"] = tier
        }
        return result
    }

    override val menuBuilder: AbstractMenu.Builder?
        get() = SBuilder(3)
                .background()
                .parentButton(4, 2) // Unexpected behaviour when clicked
                // This glitches the menu and prevents any children from being opened
                //.onClose((player) -> Result.PARENT())
                .button(3, 1, Button.Builder()
                        .lmb { interact: Button.Event ->
                            val change = if (interact.shift) 5 else 1
                            min = MathUtil.clamp(min - change, 1, min)
                            Result.refresh()
                        }
                        .rmb { interact: Button.Event ->
                            val change = if (interact.shift) 5 else 1
                            min = MathUtil.clamp(min + change, 1, max)
                            Result.refresh()
                        }
                        .icon { p: Player? ->
                            ItemBuilder.fromSkull(Editor.Companion.BASE64_DEC).name(Lang.EDITOR_COUNT_MIN).lore("""
    ${Lang.EDITOR_LMB_DECREMENT}
    ${Lang.EDITOR_INCREMENT}
    ${Lang.EDITOR_MULTIPLE}
    """.trimIndent()).amount(min).build()
                        }) // Max
                .button(5, 1, Button.Builder()
                        .lmb { interact: Button.Event ->
                            val change = if (interact.shift) 5 else 1
                            max = MathUtil.clamp(max - change, min, getRenderIcon(null).getMaxStackSize())
                            Result.refresh()
                        }
                        .rmb { interact: Button.Event ->
                            val change = if (interact.shift) 5 else 1
                            max = MathUtil.clamp(max + change, min, getRenderIcon(null).getMaxStackSize())
                            Result.refresh()
                        }
                        .icon { p: Player? ->
                            ItemBuilder.fromSkull(Editor.Companion.BASE64_INC).name(Lang.EDITOR_COUNT_MAX).lore("""
    ${Lang.EDITOR_LMB_DECREMENT}
    ${Lang.EDITOR_INCREMENT}
    ${Lang.EDITOR_MULTIPLE}
    """.trimIndent()).amount(max).build()
                        }) // Type/Name menu:
                .childButton(4, 1, { p: Player? -> ItemBuilder.mut(getRenderIcon(null)).amount(1).name(Lang.ASSIGN_EXACT).lore("$type:$name").build() }, TBuilder()
                        .title { p: Player? -> Lang.ASSIGN_EXACT }
                        .leftRaw { p: Player? -> "$type:$name" }
                        .right { p: Player? -> Lang.MMO_ENTER }
                        .onClose { player: Player? -> Result.parent() }
                        .onComplete { p: Player?, s: String, b: TBuilder? ->
                            try {
                                val split = s.uppercase(Locale.getDefault()).split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val itemStack = MMOItems.plugin.getItem(
                                        MMOItems.plugin.types[split[0]], split[1])
                                if (itemStack != null) {
                                    type = split[0]
                                    name = split[1]
                                    return@onComplete Result.parent()
                                }
                            } catch (e: Exception) {
                                //e.printStackTrace();
                            }
                            Result.text(Lang.COMMAND_ERROR_INPUT)
                        }
                ) //special delimiter reader/writer for
                // 0: Level, Tier
                // 1: Random
                // 2: Scale with player
                .childButton(7, 1, { p: Player? -> ItemBuilder.copy(Material.PAINTING).amount(1).name(Lang.MMO_EDIT_TIERS).lore(formatString).build() }, TBuilder()
                        .title { p: Player? -> Lang.LOOT_MMO_EDIT_TITLE }
                        .leftRaw { p: Player? -> formatString }
                        .right({ p: Player? -> Lang.MMO_FORMAT }) { p: Player? ->
                            """${Lang.MMO_FORMAT_LORE}
&7- exact:2,RARE
 &7- random
 &7- scale"""
                        }
                        .onClose { player: Player? -> Result.parent() }
                        .onComplete { p: Player?, s: String, b: TBuilder? ->
                            var s = s
                            s = s.replace(" ", "")
                            try {
                                val m = MODE_PATTERN.matcher(s)
                                if (m.matches()) {

                                    /*
                                     * Here I perform some deductive magic (constant expressions)
                                     * after the pattern does all the hard work
                                     */

                                    // then extract
                                    var sub = s.substring(m.start(), m.end()) // exact:23,6
                                    if (sub[0] == 'e') { // exact
                                        mode = 0
                                        var index = sub.indexOf(":") // 5
                                        sub = sub.substring(index + 1) // 23,6
                                        index = sub.indexOf(",") // 2
                                        level = sub.substring(0, index).toInt() // 23
                                        val tier = sub.substring(index + 1) // 6
                                        if (!MMOItems.plugin.tiers.has(tier)) return@onComplete Result.text(Lang.COMMAND_ERROR_INPUT)
                                        this.tier = tier
                                    } else if (sub[0] == 'r') { // random
                                        mode = 1
                                    } else mode = 2 // scale
                                    return@onComplete Result.parent()
                                }
                            } catch (ignored: Exception) {
                            }
                            Result.text(Lang.COMMAND_ERROR_INPUT)
                        }
                )
    private val formatString: String
        private get() = if (mode == 0) "exact:$level,$tier" else if (mode == 1) "random" else "scale"

    override fun copy(): LootMMOItem {
        return LootMMOItem(this)
    }

    companion object {
        val EDITOR_ICON = ItemBuilder.copy(Material.CLOCK).name("&2Add MMOItem...").build()
        private val MODE_PATTERN = Pattern.compile("(exact:[0-9]+,[a-zA-Z]+|random|scale)")
    }
}
