package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ColorMode;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.Editor;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        //if (getIcon(null) == null)
        //    throw new NullPointerException("LootMMOItem: " + type + ":" + name + " is invalid");

        //MMOItems.plugin.getTiers().getOrThrow(tier);
    }

    @Override
    public ItemStack getIcon(Player p) {
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

    @Override
    public String toString() {
        return "&8MMOItem: &f" + type + ":" + name + "\n" +
                super.toString();
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
                .title("LootMMOItem")
                .background()
                .parentButton(4, 2)
                // Unexpected behaviour when clicked
                // This glitches the menu and prevents any children from being opened
                //.onClose((player) -> Result.PARENT())
                .button(3, 1, new Button.Builder()
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
                        .icon(() -> ItemBuilder.of("PLAYER_HEAD").name("&8&nMin").skull(Editor.BASE64_DEC).lore(Editor.LORE_LMB_NUM + "\n" + Editor.LORE_RMB_NUM + "\n" + Editor.LORE_SHIFT_NUM).amount(min).build()))
                // Max
                .button(5, 1, new Button.Builder()
                        .lmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = Util.clamp(max - change, min, getIcon(null).getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            int change = interact.shift ? 5 : 1;
                            max = Util.clamp(max + change, min, getIcon(null).getMaxStackSize());
                            return Result.REFRESH();
                        })
                        .icon(() -> ItemBuilder.of("PLAYER_HEAD").name("&8&nMax").skull(Editor.BASE64_INC).lore(Editor.LORE_LMB_NUM + "\n" + Editor.LORE_RMB_NUM + "\n" + Editor.LORE_SHIFT_NUM).amount(max).build()))

                // Type/Name menu:
                .childButton(4, 1, () -> ItemBuilder.mutable(getIcon(null)).amount(1).name2Lore(ColorMode.STRIP).name("&7Edit").build(), new TextMenu.TBuilder()
                    .title("Assign by name")
                    .leftRaw(() -> type + ":" + name, null, ColorMode.STRIP)
                    .right(() -> "&eEnter the &ltype:item")
                    .onClose((player) -> Result.PARENT())
                    .onComplete((player, s) -> {
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

                        return Result.TEXT("Invalid");
                    })
                )
                //special delimiter reader/writer for
                // 0: Level, Tier
                // 1: Random
                // 2: Scale with player
                .childButton(7, 1, () -> ItemBuilder.copyOf(Material.PAINTING).amount(1).name("&8&lEdit tiers").lore(getFormatString()).build(), new TextMenu.TBuilder()
                        .title("Assign by name")
                        .leftRaw(this::getFormatString, null, ColorMode.STRIP)
                        .right(() -> "&8Format", () -> " &7- exact:<level>,<tier>\n &7- random tiers\n &7- scale with player\n&eExamples:\n &7- exact:2,RARE\n &7- random\n &7- scale")
                        .onClose((player) -> Result.PARENT())
                        .onComplete((player, s) -> {
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
                                            return Result.TEXT("Invalid tier");

                                        this.tier = tier;

                                    } else if (sub.charAt(0) == 'r') { // random
                                        mode = 1;
                                    } else mode = 2; // scale

                                    return Result.PARENT();
                                }
                            }
                            catch (Exception ignored) {}

                            return Result.TEXT("Invalid format");
                        })
                );
    }

    private String getFormatString() {
        return (mode == 0) ? "exact:" + level + "," + tier : (mode == 1) ? "random" : "scale";
    }

}
