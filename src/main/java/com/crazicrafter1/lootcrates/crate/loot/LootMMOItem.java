package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.MathUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.Lang;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.crazicrafter1.lootcrates.Editor.*;

public class LootMMOItem extends AbstractLootItem {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.CLOCK).name("&2Add MMOItem...").build();

    public String type;
    public String name;

    public int mode;
    public int level;
    public String tier;

    //special delimiter reader/writer for
    // 0: Level, Tier
    // 1: Random
    // 2: Scale with player

    // https://git.lumine.io/mythiccraft/mmoitems/-/wikis/Main%20API%20Features
    public LootMMOItem() {
        type = "SWORD";
        name = "CUTLASS";

        mode = 0;
        level = 1;
        tier = "UNCOMMON";
    }

    public LootMMOItem(Map<String, Object> args) {
        super(args);
        this.type = (String) args.get("type");
        this.name = (String) args.get("name");

        this.mode = (int) args.get("mode");

        if (mode == 0) {
            this.level = (int) args.get("level");
            this.tier = (String) args.get("tier");
        }
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        if (mode == 0) { // exact
            ItemTier itemTier = MMOItems.plugin.getTiers().getOrThrow(tier);
            return ofRange(p, MMOItems.plugin.getItem(
                    MMOItems.plugin.getTypes().get(type), name, level, itemTier));
        } else if (mode == 1) { // random
            return ofRange(p, MMOItems.plugin.getItem(
                    MMOItems.plugin.getTypes().get(type), name));
        } else { // scale
            return ofRange(p, MMOItems.plugin.getItem(
                    MMOItems.plugin.getTypes().get(type), name, PlayerData.get(p.getUniqueId())));
        }
    }

    @NotNull
    @Override
    public ItemStack getMenuIcon(@NotNull Player p) {
        ItemTier itemTier = MMOItems.plugin.getTiers().getOrThrow(tier);
        return MMOItems.plugin.getItem(
                MMOItems.plugin.getTypes().get(type), name, level, itemTier);
    }

    @NotNull
    @Override
    public String getMenuDesc(@NotNull Player p) {
        return "&8MMOItem: &f" + type + ":" + name + "\n" +
                super.getMenuDesc(p);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("type", type);
        result.put("name", name);

        result.put("mode", mode);

        if (mode == 0) {
            result.put("level", level);
            result.put("tier", tier);
        }

        return result;
    }

    private static final Pattern MODE_PATTERN = Pattern.compile("(exact:[0-9]+,[a-zA-Z]+|random|scale)");

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new SimpleMenu.SBuilder(3)
                .background()
                .parentButton(4, 2)
                // Unexpected behaviour when clicked
                // This glitches the menu and prevents any children from being opened
                //.onClose((player) -> Result.PARENT())
                .button(3, 1, new Button.Builder()
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
                        .icon((p) -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").name(Lang.MINIMUM).skull(BASE64_DEC).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC + "\n" + Lang.SHIFT_MUL).amount(min).build()))
                // Max
                .button(5, 1, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = MathUtil.clamp(max - change, min, getRenderIcon(null).getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = MathUtil.clamp(max + change, min, getRenderIcon(null).getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .icon((p) -> ItemBuilder.fromModernMaterial("PLAYER_HEAD").name(Lang.MAXIMUM).skull(BASE64_INC).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC + "\n" + Lang.SHIFT_MUL).amount(max).build()))

                // Type/Name menu:
                .childButton(4, 1, (p) -> ItemBuilder.mutable(getRenderIcon(null)).amount(1).name(Lang.ASSIGN_EXACT).lore(type + ":" + name).build(), new TextMenu.TBuilder()
                    .title(p -> Lang.ASSIGN_EXACT)
                    .leftRaw(p -> type + ":" + name)
                    .right(p -> Lang.MMO_ENTER)
                    .onClose((player) -> Result.PARENT())
                    .onComplete((p, s, b) -> {
                        try {
                            String[] split = s.toUpperCase().split(":");

                            ItemStack itemStack = MMOItems.plugin.getItem(
                                    MMOItems.plugin.getTypes().get(split[0]), split[1]);

                            if (itemStack != null) {
                                this.type = split[0];
                                this.name = split[1];
                                return Result.PARENT();
                            }
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }

                        return Result.TEXT(Lang.ERR_INVALID);
                    })
                )
                //special delimiter reader/writer for
                // 0: Level, Tier
                // 1: Random
                // 2: Scale with player
                .childButton(7, 1, (p) -> ItemBuilder.copyOf(Material.PAINTING).amount(1).name(Lang.MMO_EDIT_TIERS).lore(getFormatString()).build(), new TextMenu.TBuilder()
                        .title(p -> "")
                        .leftRaw((p) -> this.getFormatString())
                        .right(p -> Lang.MMO_FORMAT, p -> Lang.MMO_FORMAT_LORE + ":\n&7- exact:2,RARE\n &7- random\n &7- scale")
                        .onClose((player) -> Result.PARENT())
                        .onComplete((p, s, b) -> {
                            s = s.replace(" ", "");
                            try {
                                Matcher m = MODE_PATTERN.matcher(s);
                                if (m.matches()) {

                                    /*
                                     * Here I perform some deductive magic (constant expressions)
                                     * after the pattern does all the hard work
                                     */

                                    // then extract
                                    String sub = s.substring(m.start(), m.end()); // exact:23,6
                                    if (sub.charAt(0) == 'e') { // exact
                                        mode = 0;
                                        int index = sub.indexOf(":"); // 5
                                        sub = sub.substring(index+1); // 23,6
                                        index = sub.indexOf(","); // 2

                                        level = Integer.parseInt(sub.substring(0, index)); // 23
                                        String tier = sub.substring(index+1); // 6

                                        if (!MMOItems.plugin.getTiers().has(tier))
                                            return Result.TEXT(Lang.ERR_INVALID);

                                        this.tier = tier;
                                    } else if (sub.charAt(0) == 'r') { // random
                                        mode = 1;
                                    } else mode = 2; // scale

                                    return Result.PARENT();
                                }
                            }
                            catch (Exception ignored) {}

                            return Result.TEXT(Lang.ERR_INVALID);
                        })
                );
    }

    private String getFormatString() {
        return (mode == 0) ? "exact:" + level + "," + tier : (mode == 1) ? "random" : "scale";
    }

}
