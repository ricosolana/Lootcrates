package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ui.*;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import com.crazicrafter1.lootcrates.crate.LootCollection;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
                +   Lang.Custom_Macros + "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "%crate_picks%" + "\n"
                +   Lang.Separate_Lore + "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "\\n")
                ;
    }

    // TODO redo fireworks menu
    public static final Button.Builder IN_OUTLINE = new Button.Builder().icon(p -> ItemBuilder.from(
            "GRAY_STAINED_GLASS_PANE").name(" ").build());

    public static final String BASE64_DEC = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM1YThhYThhNGMwMzYwMGEyYjVhNGViNmJlYjUxZDU5MDI2MGIwOTVlZTFjZGFhOTc2YjA5YmRmZTU2NjFjNiJ9fX0=";
    public static final String BASE64_INC = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFiOTVhODc1MWFlYWEzYzY3MWE4ZTkwYjgzZGU3NmEwMjA0ZjFiZTY1NzUyYWMzMWJlMmY5OGZlYjY0YmY3ZiJ9fX0=";

    public static final Pattern PRIMARY_KEY_PATTERN = Pattern.compile("(?=.*[a-z])[a-z_]+");

    public static final Pattern NON_ASCII_PATTERN = Pattern.compile("[^a-zA-Z0-9_.]+");

    //private CrateSettings clipboardCrate;
    //private LootSetSettings clipboardLootSet;
    //private ILoot clipboardILoot;

    public void open(Player p000) {
        if (p000.getGameMode() != GameMode.CREATIVE) {
            LCMain.get().notifier.warn(p000, Lang.RECOMMEND_CREATIVE);
        }

        new SimpleMenu.SBuilder(3)
                .title(p -> Lang.Editor_Title)
                .background()
                /* *************** *\
                *                   *
                * Global Crate List *
                *                   *
                \* *************** */
                .childButton(2, 1, p -> ItemBuilder.copy(Material.CHEST).name(Lang.ED_BTN_Crates).build(), new ListMenu.LBuilder()
                                .title(p -> Lang.ED_Crates_TI)
                                .parentButton(4, 5)
                                // *       *      *
                                // Add Crate button
                                // *       *      *
                                .childButton(5, 5, p -> ItemBuilder.copy(Material.END_CRYSTAL).name(Lang.ED_Crates_BTN_New).build(), new TextMenu.TBuilder()
                                        .title(p -> Lang.ED_Crates_New_TI)
                                        .leftRaw(p -> LOREM_IPSUM)
                                        .onClose((player) -> Result.parent())
                                        .onComplete((player, s, b) -> {
                                            s = NON_ASCII_PATTERN.matcher(s.replace(" ", "_")).replaceAll("").toLowerCase();

                                            if (s.isEmpty())
                                                return Result.text(Lang.ED_INVALID_ID);

                                            // if crate already exists
                                            CrateSettings crate = Lootcrates.getCrate(s);
                                            if (crate != null)
                                                return Result.text(Lang.ED_DUP_ID);

                                            Lootcrates.registerCrate(Lootcrates.createCrate(s));

                                            return Result.parent();
                                        })

                                )//.childButton()
                                .addAll((self, p00) -> {
                                    ArrayList<Button> result = new ArrayList<>();
                                    for (Map.Entry<String, CrateSettings> entry : LCMain.get().rewardSettings.crates.entrySet()) {
                                        CrateSettings crate = entry.getValue();
                                        result.add(new Button.Builder()
                                                // https://regexr.com/6fdsi
                                                .icon(p -> crate.getMenuIcon())
                                                        //.mmb(event -> { clipboardCrate = crate; return Result.REFRESH_GRAB(); })
                                                .child(self, crate.getBuilder(),
                                                        /// RMB - delete crate
                                                        interact -> {
                                                            // TODO right-click used to delete the crate
                                                            //Main.get().rewardSettings.crates.remove(crate.id);
                                                            //return Result.REFRESH();

                                                            if (interact.shift) {
                                                                // delete crate then
                                                                LCMain.get().rewardSettings.crates.remove(crate.id);
                                                            } else {
                                                                CrateSettings copy = crate.copy();
                                                                Lootcrates.registerCrate(copy);
                                                            }
                                                            return Result.refresh();
                                                        }
                                                ).get()
                                        );
                                    }
                                    return result;
                                })
                /*
                 * View LootSets
                 */
                ).childButton(4, 1, p -> ItemBuilder.from("EXPERIENCE_BOTTLE").name(Lang.ED_BTN_LootSets).build(), new ListMenu.LBuilder()
                        .title(p -> Lang.ED_LootSets_TI)
                        .parentButton(4, 5)
                        /*
                         * Each Collection
                         */
                        .addAll((self, p1) -> {
                            ArrayList<Button> result = new ArrayList<>();
                            for (LootCollection lootSet : LCMain.get().rewardSettings.lootSets.values()) {
                                /*
                                 * Add Collections
                                 */
                                result.add(new Button.Builder()
                                        .icon(p -> lootSet.getMenuIcon())
                                        .child(self, lootSet.getBuilder(), // LMB - Edit Loot Collection
                                                // RMB - delete lootSet
                                                interact -> {
                                                    if (interact.shift) {
                                                        //if (Main.get().rewardSettings.lootSets.size() > 1) {
                                                        //    Main.get().rewardSettings.lootSets.remove(lootSet.id);
                                                        //    for (CrateSettings crate : Main.get().rewardSettings.crates.values()) {
                                                        //        crate.removeLootSet(lootSet.id);
                                                        //    }
                                                        //} else
                                                        if (Lootcrates.removeLootSet(lootSet.id))
                                                            return Result.message("Failed to remove LootSet");
                                                        return null;
                                                    } else {
                                                        LootCollection copy = lootSet.copy();
                                                        LCMain.get().rewardSettings.lootSets.put(copy.id, copy);
                                                    }

                                                    //Main.get().notifier.info(interact.player, "Copied crate to clipboard");
                                                    return Result.refresh();

                                                    //if (Main.get().rewardSettings.lootSets.size() > 1) {
                                                    //    Main.get().rewardSettings.lootSets.remove(lootSet.id);
                                                    //    for (CrateSettings crate : Main.get().rewardSettings.crates.values()) {
                                                    //        crate.loot.remove(lootSet);
                                                    //    }
                                                    //    return Result.REFRESH();
                                                    //}
                                                    //return null;
                                                }
                                        ).get()
                                );
                            }
                            return result;
                        })
                        /*
                         * Add custom Collection
                         */
                        .childButton(5, 5, p -> ItemBuilder.copy(Material.NETHER_STAR).name(Lang.ED_LootSets_BTN_New).build(), new TextMenu.TBuilder()
                                .title(p -> Lang.ED_LootSets_New_TI)
                                .leftRaw(p -> LOREM_IPSUM) // id
                                .onClose((player) -> Result.parent())
                                .onComplete((player, s, b) -> {
                                    s = NON_ASCII_PATTERN.matcher(s.replace(" ", "_")).replaceAll("").toLowerCase();

                                    if (s.isEmpty())
                                        return Result.text(Lang.ED_INVALID_ID);

                                    if (LCMain.get().rewardSettings.crates.containsKey(s))
                                        return Result.text(Lang.ED_DUP_ID);

                                    LCMain.get().rewardSettings.lootSets.put(s,
                                            new LootCollection(s, new ItemStack(Material.GLOWSTONE_DUST),
                                                    new ArrayList<>(Collections.singletonList(new LootItem()))));

                                    return Result.parent();
                                })
                        )
                )
                /*
                 * Global Fireworks Edit
                 */
                // TODO this menu is absolutely ugly in every conceivable way
                //  and requires a long overdue rework
                .childButton(6, 1, p -> ItemBuilder.from("FIREWORK_ROCKET").name(Lang.ED_BTN_Firework).build(), new SimpleMenu.SBuilder(5)
                        .title(p -> Lang.ED_Firework_TI)
                        .background()
                        .button(4, 0, IN_OUTLINE)
                        .button(3, 1, IN_OUTLINE)
                        .button(5, 1, IN_OUTLINE)
                        .button(4, 2, IN_OUTLINE)
                        .button(1, 1, new Button.Builder()
                                .icon(p -> ItemBuilder.from("FIREWORK_STAR").fireworkEffect(LCMain.get().rewardSettings.fireworkEffect).build()))
                        .button(4, 1, new Button.Builder()
                                .icon(p -> ItemBuilder.from("FIREWORK_STAR").fireworkEffect(LCMain.get().rewardSettings.fireworkEffect).build())
                                .lmb(interact -> {
                                    if (interact.heldItem != null) {
                                        //if (interact.heldItem.getItemMeta() instanceof FireworkEffectMeta meta && meta.hasEffect()) {
                                        if (interact.heldItem.getItemMeta() instanceof FireworkEffectMeta) {
                                            FireworkEffectMeta meta = (FireworkEffectMeta) interact.heldItem.getItemMeta();
                                            if (meta.hasEffect()) {
                                                LCMain.get().rewardSettings.fireworkEffect = meta.getEffect();
                                                return Result.refresh();
                                            }
                                        }
                                        interact.player.sendMessage(Lang.ED_Firework_ERROR);
                                    }

                                    return Result.grab();
                                }))
                        .parentButton(4, 4)
                )
                .open(p000);

    }

}
