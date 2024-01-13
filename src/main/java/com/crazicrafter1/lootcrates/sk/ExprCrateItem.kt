package com.crazicrafter1.lootcrates.sk

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import com.crazicrafter1.lootcrates.Lootcrates
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

class ExprCrateItem : SimpleExpression<ItemStack?>() {
    private var crateExpr: Expression<String>? = null
    override fun init(exprs: Array<Expression<*>?>, i: Int, kleenean: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        crateExpr = exprs[0] as Expression<String>?
        return true
    }

    override fun get(event: Event): Array<ItemStack?> {
        val crateName = crateExpr!!.getSingle(event)
        if (crateName != null) {
            val crate = Lootcrates.getCrate(crateName) ?: return null
            return if (event is PlayerEvent) arrayOf(crate.itemStack(event.player)) else arrayOf(crate.itemStack(null))

            // else the raw item is returned
        }
        return null
    }

    override fun toString(event: Event?, b: Boolean): String {
        return "ExprCrateItem with expression string: " + crateExpr!!.toString(event, b)
    }

    override fun isSingle(): Boolean {
        return true
    }

    override fun getReturnType(): Class<out ItemStack?> {
        return ItemStack::class.java
    }

    companion object {
        init {
            Skript.registerExpression(ExprCrateItem::class.java, ItemStack::class.java, ExpressionType.COMBINED, "crate %string%")
        }
    }
}
