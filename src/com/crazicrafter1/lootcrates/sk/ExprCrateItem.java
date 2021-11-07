package com.crazicrafter1.lootcrates.sk;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public class ExprCrateItem extends SimpleExpression<ItemStack> {

    static {
        //register(ExprCrateItem.class, ItemStack.class,
        //        "crate", "string");
        //Skript.registerExpression(ExprCrateItem.class, ItemStack.class, ExpressionType.PROPERTY,
        //        "crate %string%");
        Skript.registerExpression(ExprCrateItem.class, ItemStack.class, ExpressionType.COMBINED, "crate %string%");
    }

    private Expression<String> crateExpr;

    @Override
    public boolean init(Expression<?>[] exprs, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        crateExpr = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected ItemStack[] get(Event event) {
        String crateName = crateExpr.getSingle(event);
        if (crateName != null) {
            return new ItemStack[] {
                    LootCratesAPI.getCrateByID(crateName)
                            .itemStack.clone()};
        }
        return null;
    }

    @Override
    public String toString(Event event, boolean b) {
        return "ExprCrateItem with expression string: " + crateExpr.toString(event, b);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }
}
