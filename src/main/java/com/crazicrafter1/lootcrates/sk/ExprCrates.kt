package com.crazicrafter1.lootcrates.sk

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import com.crazicrafter1.lootcrates.LCMain
import org.bukkit.event.Event

class ExprCrates : SimpleExpression<String?>() {
    //private Expression<String> crateExpr;
    override fun init(exprs: Array<Expression<*>?>?, i: Int, kleenean: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        //crateExpr = (Expression<String>) exprs[0];
        //Main.get().data.crates.keySet()
        return true
    }

    override fun get(event: Event): Array<String?> {
        val list: List<String?> = ArrayList<String?>(LCMain.Companion.get()!!.rewardSettings!!.crates!!.keys)
        return list.toTypedArray<String?>()
    }

    override fun toString(event: Event?, b: Boolean): String {
        return "ExprCrates"
    }

    override fun isSingle(): Boolean {
        return true
    }

    override fun getReturnType(): Class<out String?> {
        return String::class.java
    }

    companion object {
        init {
            //register(ExprCrateItem.class, ItemStack.class,
            //        "crate", "string");
            //Skript.registerExpression(ExprCrateItem.class, ItemStack.class, ExpressionType.PROPERTY,
            //        "crate %string%");
            Skript.registerExpression(ExprCrates::class.java, String::class.java, ExpressionType.COMBINED, "crates")
        }
    }
}
