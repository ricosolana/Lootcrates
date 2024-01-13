package com.crazicrafter1.lootcrates.sk

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import com.crazicrafter1.lootcrates.Lootcrates
import org.bukkit.entity.Player
import org.bukkit.event.Event

class EffOpenCrate : Effect() {
    private var crate: Expression<String>? = null
    private var player: Expression<Player>? = null
    override fun init(exprs: Array<Expression<*>?>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        crate = exprs[0] as Expression<String>?
        player = exprs[1] as Expression<Player>?
        return true
    }

    override fun execute(e: Event) {
        val id = crate!!.getSingle(e)
        val p = player!!.getSingle(e)
        Lootcrates.showCrate(p, Lootcrates.getCrate(id))
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return "opencrate " + crate!!.toString(e, debug) + " to " + player!!.toString(e, debug)
    }

    companion object {
        init {
            Skript.registerEffect(EffOpenCrate::class.java,
                    "opencrate %string% to %player%")
        }
    }
}
