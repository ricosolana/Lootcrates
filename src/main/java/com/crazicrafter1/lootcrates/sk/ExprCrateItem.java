package com.crazicrafter1.lootcrates.sk;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.crazicrafter1.lootcrates.LootcratesAPI;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class ExprCrateItem extends SimpleExpression<ItemStack> {

    static {
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
            CrateSettings crate = LootcratesAPI.getCrateByID(crateName);
            if (crate == null) return null;
            if (event instanceof PlayerEvent)
                return new ItemStack[] { crate.itemStack(((PlayerEvent) event).getPlayer()) };

            // else the raw item is returned
            return new ItemStack[] { crate.itemStack(null) };
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
