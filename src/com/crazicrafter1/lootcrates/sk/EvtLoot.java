package com.crazicrafter1.lootcrates.sk;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class EvtLoot extends SkriptEvent {

    static {
        Skript.registerEvent("Loot Execute", EvtLoot.class, SkriptLootEvent.class, "loot %string%");

        // Create the 'player' macro
        EventValues.registerEventValue(SkriptLootEvent.class, Player.class, new Getter<Player, SkriptLootEvent>() {
            @Override
            public Player get(SkriptLootEvent e) {
                return e.getPlayer();
            }
        }, 0);
    }

    private String tag = null;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        tag = ((Literal<String>)args[0]).getSingle();
        return true;
    }

    @Override
    public boolean check(Event evt) {
        final SkriptLootEvent e = (SkriptLootEvent) evt;

        return e.getTag().equals(this.tag);
    }

    @Override
    public String toString(Event event, boolean b) {
        return "Loot execute event";
    }
}
