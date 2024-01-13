package com.crazicrafter1.lootcrates.sk

import ch.njol.skript.Skript
import ch.njol.skript.lang.Literal
import ch.njol.skript.lang.SkriptEvent
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.registrations.EventValues
import ch.njol.skript.util.Getter
import org.bukkit.entity.Player
import org.bukkit.event.Event

class EvtLoot : SkriptEvent() {
    private var tag: String? = null
    override fun init(args: Array<Literal<*>>, matchedPattern: Int, parseResult: SkriptParser.ParseResult): Boolean {
        tag = (args[0] as Literal<String?>).single
        return true
    }

    override fun check(evt: Event): Boolean {
        val e = evt as SkriptLootEvent
        return e.tag == tag
    }

    override fun toString(event: Event?, b: Boolean): String {
        return "Loot execute event"
    }

    companion object {
        init {
            Skript.registerEvent("Loot Execute", EvtLoot::class.java, SkriptLootEvent::class.java, "loot %string%")

            // Create the 'player' macro
            EventValues.registerEventValue(SkriptLootEvent::class.java, Player::class.java, object : Getter<Player?, SkriptLootEvent>() {
                override fun get(e: SkriptLootEvent): Player? {
                    return e.player
                }
            }, 0)
        }
    }
}
