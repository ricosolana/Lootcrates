package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.MathUtil;
import com.crazicrafter1.crutils.RandomUtil;
import com.crazicrafter1.crutils.ui.*;
import com.crazicrafter1.lootcrates.Editor;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractLootItem implements ILoot {

    public int min;
    public int max;

    public AbstractLootItem() {
        min = 1;
        max = 1;
    }

    public AbstractLootItem(Map<String, Object> args) {
        this.min = (int) args.get("min");
        this.max = (int) args.get("max");

        if (min > max)
            throw new IllegalArgumentException("failed to assert: min <= max");
    }

    @Override
    public final boolean execute(@Nonnull CrateInstance activeCrate) {
        return true;
    }

    @Nonnull
    public String getMenuDesc() {
        StringBuilder sb = new StringBuilder();
        if (min == max)
            sb.append(String.format(Lang.ITEM_COUNT, min));
        else sb.append(String.format(Lang.ITEM_RANGE, min, max));

        return sb.toString();
    }

    @Nonnull
    protected ItemStack ofRange(@Nonnull Player p, @Nonnull ItemStack itemStack) {
        return ItemBuilder.copy(itemStack)
                .amount(RandomUtil.randomRange(min, max))
                .placeholders(p)
                .renderAll()
                .build();
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("min", min);
        result.put("max", max);

        return result;
    }

    protected SimpleMenu.SBuilder rangeButtons(SimpleMenu.SBuilder builder, ItemStack itemStack,
                                          int x1, int y1, int x2, int y2) {
        return builder.button(x1, y1, new Button.Builder()
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
                .icon(p -> ItemBuilder.from("PLAYER_HEAD").name(Lang.MINIMUM).skull(Editor.BASE64_DEC).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC + "\n" + Lang.SHIFT_MUL).amount(min).build()))
                // Max
                .button(x2, y2, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = MathUtil.clamp(max - change, min, itemStack.getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = MathUtil.clamp(max + change, min, itemStack.getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .icon(p -> ItemBuilder.from("PLAYER_HEAD").name(Lang.MAXIMUM).skull(Editor.BASE64_INC).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC + "\n" + Lang.SHIFT_MUL).amount(max).build()));

    }

    /*
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
