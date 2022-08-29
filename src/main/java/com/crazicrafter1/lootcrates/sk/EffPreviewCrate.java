package com.crazicrafter1.lootcrates.sk;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.crazicrafter1.lootcrates.LootcratesAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class EffPreviewCrate extends Effect {

    static {
        Skript.registerEffect(EffPreviewCrate.class,
                "previewcrate %string% to %player%");
    }

    private Expression<String> crate;
    private Expression<Player> player;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        crate = (Expression<String>) exprs[0];
        player = (Expression<Player>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event e) {
        String id = crate.getSingle(e);
        Player p = player.getSingle(e);
        LootcratesAPI.displayCratePreview(p, id);
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "previewcrate " + crate.toString(e, debug) + " to " + player.toString(e, debug);
    }
}
