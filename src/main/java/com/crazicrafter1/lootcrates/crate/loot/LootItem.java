package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.lootcrates.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static com.crazicrafter1.lootcrates.Lang.L;

public final class LootItem extends AbstractLootItem {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.GOLD_NUGGET).name("&6Add item...").build();

    public ItemStack itemStack;

    /**
     * Default ctor
     */
    public LootItem() {
        this.itemStack = new ItemStack(Material.STONE);
    }

    public LootItem(Map<String, Object> args) {
        super(args);

        int rev = Main.get().rev;
        if (rev < 2)
            this.itemStack = (ItemStack) args.get("itemStack");
        else if (rev == 2)
            this.itemStack = ((MinItemStack) args.get("itemStack")).get().build();

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            //Main.get().error(args.toString());
            throw new NullPointerException("Item must not be null or air");
        }
    }

    @Override
    public ItemStack getIcon(Player p) {
        return super.ofRange(p, itemStack);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("itemStack", new MinItemStack(itemStack));
        return result;
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        //Button.Builder inOutline = new Button.Builder().icon(() -> new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&7Set to").toItem());
        return new ItemModifyMenu()
                .build(itemStack, input -> this.itemStack = input)
                // Min
                .button(5, 2, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            min = Util.clamp(min - change, 1, min);
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            min = Util.clamp(min + change, 1, max);
                            return Result.REFRESH();
                        })
                        .icon(p -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").name("&8&n" + L(p, Lang.A.Minimum)).skull(Editor.BASE64_DEC).lore(L(Lang.A.LMB) + " &c-\n" +  L(Lang.A.RMB) + " &a+\n&7" + L(Lang.A.SHIFT_Mul) + "&r&7: x5").amount(min).build()))
                // Max
                .button(7, 2, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = Util.clamp(max - change, min, itemStack.getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = Util.clamp(max + change, min, itemStack.getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .icon(p -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").name("&8&n" + L(p, Lang.A.Maximum)).skull(Editor.BASE64_INC).lore(L(Lang.A.LMB) + " &c-\n" +  L(Lang.A.RMB) + " &a+\n&7" + L(Lang.A.SHIFT_Mul) + "&r&7: x5").amount(max).build()));
    }
}
