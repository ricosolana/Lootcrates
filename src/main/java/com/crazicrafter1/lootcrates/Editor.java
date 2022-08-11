package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ui.*;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import com.crazicrafter1.lootcrates.crate.LootSetSettings;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import java.util.*;
import java.util.regex.Pattern;

public class Editor {

    public static final String LOREM_IPSUM = "Lorem ipsum";
    private static final String COLORS = ColorUtil.renderMarkers("&a" + LOREM_IPSUM) +                                  "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "&a" + LOREM_IPSUM +                                    "\n"
            +   ColorUtil.renderMarkers("&#456789" + LOREM_IPSUM) +                                                     "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "&#456789" + LOREM_IPSUM +                              "\n"
            +   ColorUtil.renderAll("<#aa7744>" + LOREM_IPSUM + "</#abcdef>") +                                         "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "<#aa7744>" + LOREM_IPSUM + "</#abcdef>" +              "\n"
            +   ColorUtil.renderAll("<DEEP_CHESTNUT>" + LOREM_IPSUM + "</DARK_KHAKI>") +                                "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "<DEEP_CHESTNUT>" + LOREM_IPSUM + "</DARK_KHAKI>" +     "\n"
            +   ColorUtil.renderAll("<CHERRY_BLOSSOM_PINK>" + LOREM_IPSUM + "</#bb4477>") +                             "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "<CHERRY_BLOSSOM_PINK>" + LOREM_IPSUM + "</#bb4477>" +  "\n"
            +   ColorUtil.renderAll("<#555555>&8-------&7------&f---</#bbbbbb><#bbbbbb>&f---&7------&8-------</#555555>") +                                                                                                         "\n"
            ;

    public static String getColorDem() {
        return COLORS
                +   ColorUtil.renderAll(String.format(Lang.SUPPORT_PLUGIN_X, "PlaceholderAPI") +            "\n"
                +   Lang.CUSTOM_MACROS + "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "%crate_picks%" + "\n"
                +   Lang.SEPARATE_LORE + "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "\\n")
                ;
    }

    // TODO improve firework menu (tacky and plain ATM)
    public static final Button.Builder IN_OUTLINE = new Button.Builder().icon(p -> ItemBuilder.from(
            "GRAY_STAINED_GLASS_PANE").name(" ").build());

    public static final String BASE64_DEC = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM1YThhYThhNGMwMzYwMGEyYjVhNGViNmJlYjUxZDU5MDI2MGIwOTVlZTFjZGFhOTc2YjA5YmRmZTU2NjFjNiJ9fX0=";
    public static final String BASE64_INC = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFiOTVhODc1MWFlYWEzYzY3MWE4ZTkwYjgzZGU3NmEwMjA0ZjFiZTY1NzUyYWMzMWJlMmY5OGZlYjY0YmY3ZiJ9fX0=";

    public static final Pattern PRIMARY_KEY_PATTERN = Pattern.compile("(?=.*[a-z])[a-z_]+");

    public static final Pattern NON_ASCII_PATTERN = Pattern.compile("[^a-zA-Z0-9_.]+");

    private CrateSettings clipboardCrate;
    private LootSetSettings clipboardLootSet;
    private ILoot clipboardILoot;

    public void open(Player p000) {
        new SimpleMenu.SBuilder(3)
                .title(p -> Lang.TITLE_EDITOR)
                .background()
                /* *************** *\
                *                   *
                * Global Crate List *
                *                   *
                \* *************** */
                .childButton(2, 1, p -> ItemBuilder.copy(Material.CHEST).name(Lang.BUTTON_CRATES).build(), new ParallaxMenu.PBuilder()
                                .title(p -> Lang.TITLE_CRATES)
                                .parentButton(4, 5)
                                // *       *      *
                                // Add Crate button
                                // *       *      *
                                .childButton(5, 5, p -> ItemBuilder.copy(Material.NETHER_STAR).name(Lang.BUTTON_NEW_CRATE).build(), new TextMenu.TBuilder()
                                        .title(p -> Lang.TITLE_NEW_CRATE)
                                        .leftRaw(p -> LOREM_IPSUM)
                                        .right(p -> Lang.CRATE_FORMAT, p -> Lang.CRATE_FORMAT_LORE)
                                        .onClose((player) -> Result.PARENT())
                                        .onComplete((player, s, b) -> {
                                            s = NON_ASCII_PATTERN.matcher(s.replace(" ", "_")).replaceAll("").toLowerCase();

                                            if (s.isEmpty())
                                                return Result.TEXT(Lang.INVALID_ID);

                                            if (Main.get().rewardSettings.crates.containsKey(s))
                                                return Result.TEXT(Lang.DUPLICATE_ID);

                                            //if (!PRIMARY_KEY_PATTERN.matcher(s).matches() || Main.get().data.crates.containsKey(s))
                                            //    return Result.TEXT(Lang.DUPLICATE);

                                            CrateSettings crate = new CrateSettings(s);
                                            crate.loot.add(Main.get().rewardSettings.lootSets.values().iterator().next(), 1);

                                            Main.get().rewardSettings.crates.put(s, crate);

                                            return Result.PARENT();
                                        })
                                )
                                .addAll((self, p00) -> {
                                    ArrayList<Button> result = new ArrayList<>();
                                    for (Map.Entry<String, CrateSettings> entry : Main.get().rewardSettings.crates.entrySet()) {
                                        CrateSettings crate = entry.getValue();
                                        result.add(new Button.Builder()
                                                // https://regexr.com/6fdsi
                                                .icon(p -> ItemBuilder.copy(crate.item).renderAll().lore(String.format(Lang.FORMAT_ID, crate.id) + "\n" + Lang.LMB_EDIT + "\n" + Lang.RMB_DELETE).build())
                                                        .mmb(event -> { clipboardCrate = crate; return Result.REFRESH_GRAB(); })
                                                .child(self, crate.getBuilder(),
                                                        /// RMB - delete crate
                                                        interact -> {
                                                            Main.get().rewardSettings.crates.remove(crate.id);
                                                            return Result.REFRESH();
                                                        }
                                                ).get()
                                        );
                                    }
                                    return result;
                                })
                        /*
                         * View LootSets
                         */
                ).childButton(4, 1, p -> ItemBuilder.from("EXPERIENCE_BOTTLE").name(Lang.BUTTON_LOOT_SETS).build(), new ParallaxMenu.PBuilder()
                        .title(p -> Lang.TITLE_LOOT_SETS)
                        .parentButton(4, 5)
                        .addAll((self, p1) -> {
                            ArrayList<Button> result = new ArrayList<>();
                            for (LootSetSettings lootSet : Main.get().rewardSettings.lootSets.values()) {
                                /*
                                 * List all LootSets
                                 */
                                result.add(new Button.Builder()
                                        .icon(p -> ItemBuilder.copy(lootSet.item).lore(String.format(Lang.FORMAT_ID, lootSet.id) + "\n" + String.format(Lang.LOOT_SET_COUNT, lootSet.loot.size()) + "\n" + Lang.LMB_EDIT + "\n" + Lang.RMB_DELETE).build())
                                        .child(self, lootSet.getBuilder(),
                                                // RMB - delete lootSet
                                                interact -> {
                                                    if (Main.get().rewardSettings.lootSets.size() > 1) {
                                                        Main.get().rewardSettings.lootSets.remove(lootSet.id);
                                                        for (CrateSettings crate : Main.get().rewardSettings.crates.values()) {
                                                            crate.loot.remove(lootSet);
                                                        }
                                                        return Result.REFRESH();
                                                    }
                                                    return null;
                                                }
                                        ).get()
                                );
                            }
                            return result;
                        })
                        .childButton(5, 5, p -> ItemBuilder.copy(Material.NETHER_STAR).name(Lang.BUTTON_NEW_LOOT_SET).build(), new TextMenu.TBuilder()
                                .title(p -> Lang.TITLE_NEW_LOOT_SET)
                                .leftRaw(p -> LOREM_IPSUM) // id
                                .onClose((player) -> Result.PARENT())
                                .onComplete((player, s, b) -> {
                                    s = NON_ASCII_PATTERN.matcher(s.replace(" ", "_")).replaceAll("").toLowerCase();

                                    if (s.isEmpty())
                                        return Result.TEXT(Lang.INVALID_ID);

                                    if (Main.get().rewardSettings.crates.containsKey(s))
                                        return Result.TEXT(Lang.DUPLICATE_ID);

                                    //if (!PRIMARY_KEY_PATTERN.matcher(s).matches() || Main.get().data.lootSets.containsKey(s))
                                    //    return Result.TEXT(Lang.DUPLICATE);

                                    Main.get().rewardSettings.lootSets.put(s,
                                            new LootSetSettings(s, new ItemStack(Material.GLOWSTONE_DUST),
                                                    new ArrayList<>(Collections.singletonList(new LootItem()))));

                                    return Result.PARENT();
                                })
                        )
                )
                /*
                 * Global Fireworks Edit
                 */
                .childButton(6, 1, p -> ItemBuilder.from("FIREWORK_ROCKET").name(Lang.BUTTON_EDIT_FIREWORK).build(), new SimpleMenu.SBuilder(5)
                        .title(p -> Lang.TITLE_FIREWORK)
                        .background()
                        .button(4, 0, IN_OUTLINE)
                        .button(3, 1, IN_OUTLINE)
                        .button(5, 1, IN_OUTLINE)
                        .button(4, 2, IN_OUTLINE)
                        .button(1, 1, new Button.Builder()
                                .icon(p -> ItemBuilder.from("FIREWORK_STAR").fireworkEffect(Main.get().rewardSettings.fireworkEffect).build()))
                        .button(4, 1, new Button.Builder()
                                .icon(p -> ItemBuilder.from("FIREWORK_STAR").fireworkEffect(Main.get().rewardSettings.fireworkEffect).build())
                                .lmb(interact -> {
                                    if (interact.heldItem != null) {
                                        //if (interact.heldItem.getItemMeta() instanceof FireworkEffectMeta meta && meta.hasEffect()) {
                                        if (interact.heldItem.getItemMeta() instanceof FireworkEffectMeta) {
                                            FireworkEffectMeta meta = (FireworkEffectMeta) interact.heldItem.getItemMeta();
                                            if (meta.hasEffect()) {
                                                Main.get().rewardSettings.fireworkEffect = meta.getEffect();
                                                return Result.REFRESH();
                                            }
                                        }
                                        interact.player.sendMessage(Lang.REQUIRE_FIREWORK_EFFECT);
                                    }

                                    return Result.GRAB();
                                }))
                        .parentButton(4, 4)
                )
                .open(p000);

    }

}
