package com.crazicrafter1.lootcrates.crate.loot

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.crutils.ui.SimpleMenu.SBuilder
import com.crazicrafter1.lootcrates.*
import com.crazicrafter1.lootcrates.crate.CrateInstance
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class AbstractLootItem protected constructor(var min: Int, var max: Int) : ILoot {
    constructor() : this(1, 1)
    constructor(args: Map<String?, Any?>) : this(args["min"] as Int, args["max"] as Int) {
        require(min <= max) { "failed to assert: min <= max" }
    }

    protected constructor(other: AbstractLootItem) : this(other.min, other.max)

    //public static AbstractLootItem deserialize(Map<String, Object> args) {
    //    int min = (int) args.get("min");
    //    int max = (int) args.get("max");
    //    if (min > max)
    //        throw new IllegalArgumentException("failed to assert: min <= max");
    //
    //    return new
    //}
    override fun execute(activeCrate: CrateInstance): Boolean {
        return true
    }

    // treated as a virtually overrideable object
    //  where getter is only used, no setter?
    override val menuDesc: String
        get() {
            val sb = StringBuilder()
            if (min == max) sb.append(String.format(Lang.EDITOR_LOOT_COUNT, min)) else sb.append(String.format(Lang.EDITOR_LOOT_RANGE, min, max))
            return sb.toString()
        }

    protected fun ofRange(p: Player, itemStack: ItemStack): ItemStack {
        return ItemBuilder.copy(itemStack)
                .amount(RandomUtil.randomRange(min, max))
                .placeholders(p)
                .renderAll()
                .build()
    }

    override fun serialize(): MutableMap<String, Any> {
        val result: MutableMap<String, Any> = LinkedHashMap()
        result["min"] = min
        result["max"] = max
        return result
    }

    protected fun rangeButtons(builder: SBuilder, itemStack: ItemStack,
                               x1: Int, y1: Int, x2: Int, y2: Int): SBuilder {
        return builder.button(x1, y1, Button.Builder()
                .click { e: Button.Event ->
                    val clickType = e.clickType
                    if (!(clickType.isLeftClick || clickType.isRightClick)) return@click Result.ok()
                    var change = if (e.shift) 5 else 1
                    if (clickType.isLeftClick) change *= -1
                    min = MathUtil.clamp(min + change, 1, max)
                    Result.refresh()
                }
                .icon { p: Player? ->
                    ItemBuilder.fromSkull(Editor.BASE64_DEC).name(Lang.EDITOR_COUNT_MIN).lore("""
    ${Lang.EDITOR_LMB_DECREMENT}
    ${Lang.EDITOR_INCREMENT}
    ${Lang.EDITOR_MULTIPLE}
    """.trimIndent()).amount(min).build()
                }) // Max
                .button(x2, y2, Button.Builder()
                        .click { e: Button.Event ->
                            val clickType = e.clickType
                            if (!(clickType.isLeftClick || clickType.isRightClick)) return@click Result.ok()
                            var change = if (e.shift) 5 else 1
                            if (clickType.isLeftClick) change *= -1
                            max = MathUtil.clamp(max + change, min, itemStack.getMaxStackSize())
                            Result.refresh()
                        }
                        .icon { p: Player? ->
                            ItemBuilder.fromSkull(Editor.BASE64_INC).name(Lang.EDITOR_COUNT_MAX).lore("""
    ${Lang.EDITOR_LMB_DECREMENT}
    ${Lang.EDITOR_INCREMENT}
    ${Lang.EDITOR_MULTIPLE}
    """.trimIndent()).amount(max).build()
                        })
    } /*
    @NotNull
    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ItemModifyMenu()
                //.build(item.build(), input -> (this.item = ItemBuilder.mutable(input)).build())
                // Min
                .button(3, 0, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            min = MathUtil.clamp(min - change, 1, min);
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            min = MathUtil.clamp(min + change, 1, max);
                            return Result.REFRESH();
                        })
                        .icon(p -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").name(Lang.MINIMUM).skull(Editor.BASE64_DEC).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC + "\n" + Lang.SHIFT_MUL).amount(min).build()))
                // Max
                .button(5, 0, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = MathUtil.clamp(max - change, min, getMenuIcon().getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = MathUtil.clamp(max + change, min, getMenuIcon().getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .icon(p -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").name(Lang.MAXIMUM).skull(Editor.BASE64_INC).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC + "\n" + Lang.SHIFT_MUL).amount(max).build()));

    }
     */
}
