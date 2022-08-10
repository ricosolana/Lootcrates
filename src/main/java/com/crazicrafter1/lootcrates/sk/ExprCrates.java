package com.crazicrafter1.lootcrates.sk;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.crazicrafter1.lootcrates.Main;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

public class ExprCrates extends SimpleExpression<String> {

    static {
        //register(ExprCrateItem.class, ItemStack.class,
        //        "crate", "string");
        //Skript.registerExpression(ExprCrateItem.class, ItemStack.class, ExpressionType.PROPERTY,
        //        "crate %string%");
        Skript.registerExpression(ExprCrates.class, String.class, ExpressionType.COMBINED, "crates");
    }

    //private Expression<String> crateExpr;

    @Override
    public boolean init(Expression<?>[] exprs, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        //crateExpr = (Expression<String>) exprs[0];
        //Main.get().data.crates.keySet()
        return true;
    }

    @Override
    protected String[] get(Event event) {
        List<String> list = new ArrayList<>(Main.get().rewardSettings.crates.keySet());

        return list.toArray(new String[0]);
    }

    @Override
    public String toString(Event event, boolean b) {
        return "ExprCrates";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }
}
