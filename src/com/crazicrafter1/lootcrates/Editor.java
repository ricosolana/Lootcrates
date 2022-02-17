package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.crutils.Version;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import com.sun.istack.internal.NotNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import java.util.*;

public class Editor {

    //public static final Button.Builder inOutline = new Button.Builder().icon(() -> ItemBuilder.copyOf(Material.GRAY_STAINED_GLASS_PANE).name("&7Set to").toItem());

    public static final boolean IS_NEW = Version.AT_LEAST_v1_16.a();

    public static final Button.Builder IN_OUTLINE = new Button.Builder().icon(() -> ItemBuilder.of(
            "GRAY_STAINED_GLASS_PANE").name("&7Set to").build());

    public static final String LORE_LMB_EDIT = "&7LMB: &aEdit";
    public static final String LORE_RMB_DEL = "&7RMB: &cDelete";
    public static final String LORE_LMB_NUM = "&7LMB&r&7: &c-";
    public static final String LORE_RMB_NUM = "&7RMB&r&7: &a+";
    public static final String LORE_SHIFT_NUM = "&7SHIFT&r&7: x5";

    public static final ItemBuilder ITEM_NEW = ItemBuilder.copyOf(Material.NETHER_STAR).name("&6New");
    public static final String NAME_EDIT = "&aEdit";

    @SuppressWarnings("SpellCheckingInspection")
    public static final String BASE64_CUSTOM_MODEL_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQ2NDg3NGRmNDUyYzFkNzE3ZWRkZDBmYjNiODQ4MjAyYWQxNTU3MTI0NWFmNmZhZGUyZWNmNTE0ZjNjODBiYiJ9fX0=";

    public static String COLOR_PREFIX = "&7'&' " + (IS_NEW ?
            "or '#&': &#2367fbc&#3f83fbo&#5a9ffcl&#76bbfco&#91d7fcr&#acf2fds" : ": &fcolors") +
            "\n&7Macros: &6%lc_picks%&7, &6%lc_id%\n&7Supports PlaceholderAPI";

    private static final String LOREM_IPSUM = "Lorem ipsum";

    @NotNull
    static String L(Player p, String key, String msg) {
        LanguageUnit unit = Main.get().getLang(p);
        /// If language match failed, return the default
        if (unit == null) {
            //String find = Main.get().data.editorEnglish.get(key);
            //if (find != null) {
            //    if (!find.equals(msg))
            //        throw new RuntimeException("Tried registering dup of key: " + key + " (old: " + find + ", new: " + msg);
            //}
            Main.get().data.editorEnglish.put(key, msg); // TEMPORARY for debug generation
            return msg;
        }

        // Look for the key
        // this current functionality is not good for future usability
        // as this just forge merges any absent key into ANY language spec

        // This is only included for initial add (remove for release build)
        return unit.editor.getOrDefault(key, msg);
    }

    /**
     * Non-asynchronous method for opening an editor to player
     * @param p
     */
    @SuppressWarnings("DanglingJavadoc")
    public static void open(Player p) {
        new SimpleMenu.SBuilder(3)
                .title("editor", true)
                .background()
                /*********************\
                 *                   *
                 * Global Crate List *
                 *                   *
                 \*******************/
                .childButton(1, 1, () -> ItemBuilder.copyOf(Material.CHEST).name(L(p, "Crates", "&3&lCrates")).build(), new ParallaxMenu.PBuilder()
                                .title(L(p, "crates", "crates"), true)
                                .parentButton(4, 5)
                                // *       *      *
                                // Add Crate button
                                // *       *      *
                                .childButton(5, 5, () -> ItemBuilder.copyOf(ITEM_NEW).name(L(p, "ITEM_NEW", ITEM_NEW.getName())).build(), new TextMenu.TBuilder()
                                        .title(L(p, "new_crate", "new crate"), true)
                                        .leftRaw(() -> L(p, "LOREM_IPSUM", LOREM_IPSUM))
                                        .onClose((player, reroute) -> Result.BACK())
                                        .onComplete((player, s) -> {
                                            if (s.isEmpty() || Main.get().data.crates.containsKey(s)) {
                                                return Result.TEXT(L(p, "Input_a_unique_key", "Input a unique key"));
                                            }
                                            Crate crate = new Crate(s, ItemBuilder.copyOf(Material.ENDER_CHEST).name("my new crate").build(), "select loot", 3, 4, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);

                                            crate.lootBySum = new LinkedHashMap<>();
                                            crate.lootBySum.put(Main.get().data.lootSets.values().iterator().next(), 1);
                                            crate.sumsToWeights();

                                            Main.get().data.crates.put(s, crate);

                                            return Result.BACK();
                                        })
                                )
                                .addAll(self -> {
                                    ArrayList<Button> result = new ArrayList<>();
                                    for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {
                                        Crate crate = entry.getValue();
                                        result.add(new Button.Builder()
                                                // https://regexr.com/6fdsi
                                                // (?<!\\)(\${)([^\${]+)[}]
                                                //                                                  .lore("&8id: " + lootSet.id + "\n" + "&8" + lootSet.loot.size() + " elements" + "\n" + LORE_LMB_EDIT + "\n" + LORE_RMB_DEL)
                                                .icon(() -> ItemBuilder.copyOf(crate.itemStack).lore("&8id: " + crate.id + "\n" + L(p, "LORE_LMB_EDIT", LORE_LMB_EDIT) + "\n" + L(p, "LORE_RMB_DEL", LORE_RMB_DEL)).build())
                                                .child(self, new SimpleMenu.SBuilder(5)
                                                                .title(crate.id, true)
                                                                .background()
                                                                .parentButton(4, 4)
                                                                // *   *   *
                                                                // Edit Crate ItemStack
                                                                // *   *   *
                                                                .childButton(1, 1, () -> ItemBuilder.copyOf(crate.itemStack.getType()).name(L(p, "ItemStack", "&8&nItemStack")).lore(L(p, "LORE_LMB_EDIT", LORE_LMB_EDIT)).build(), new ItemMutateMenuBuilder()
                                                                        .build(crate.itemStack.clone(), (itemStack -> {
                                                                            //Main.get().info("consumer: " + itemStack);
                                                                            crate.itemStack = itemStack;
                                                                        }))
                                                                )
                                                                // *   *   *
                                                                // Edit Inventory Title
                                                                // *   *   *
                                                                .childButton(3, 1, () -> ItemBuilder.copyOf(Material.PAPER).name("&e&n" + L(p, "Title", "Title") + "&r&e: " + crate.title).lore(L(p, "LORE_LMB_EDIT", LORE_LMB_EDIT)).build(), new TextMenu.TBuilder()
                                                                        .title(L(p, "title", "title"), true)
                                                                        .leftRaw(() -> Util.toAlternateColorCodes('&', crate.title))
                                                                        .onClose((player, reroute) -> Result.BACK())
                                                                        //.leftInput(SAMPLE_LEFT)
                                                                        .right(() -> L(p, "COLOR_PREFIX", COLOR_PREFIX))
                                                                        .onComplete((player, s) -> {
                                                                            // set name if it is not empty
                                                                            if (!s.isEmpty()) {
                                                                                crate.title = Util.format("" + s);
                                                                                return Result.BACK();
                                                                            }

                                                                            return Result.TEXT(L(p, "Invalid", "Invalid"));
                                                                        })
                                                                )
                                                                // *   *   *
                                                                // Edit LootSets
                                                                // *   *   *
                                                                .childButton(5, 1, () -> ItemBuilder.of("EXPERIENCE_BOTTLE").name(L(p, "Loot", "&6&nLoot")).lore(LORE_LMB_EDIT).build(), new ParallaxMenu.PBuilder()
                                                                        .title(L(p, "loot", "loot"), true)
                                                                        .parentButton(4, 5)
                                                                        .addAll(builder -> {
                                                                            ArrayList<Button> result1 = new ArrayList<>();

                                                                            for (LootSet lootSet : Main.get().data.lootSets.values()) {
                                                                                Integer weight = crate.lootByWeight.get(lootSet);
                                                                                Button.Builder btn = new Button.Builder();
                                                                                ItemBuilder b = ItemBuilder.copyOf(lootSet.itemStack.getType()).name("&8" + lootSet.id);
                                                                                if (weight != null) {
                                                                                    b.lore("&7" + crate.getFormattedFraction(lootSet) + "\n" +
                                                                                            "&7" + crate.getFormattedPercent(lootSet) + "\n" +
                                                                                            LORE_LMB_NUM + "\n" +
                                                                                            LORE_RMB_NUM + "\n" +
                                                                                            "&fMMB: &7" + L(p, "toggle", "toggle") + "\n" +
                                                                                            "&fShift: &7x5").glow(true);
                                                                                    btn.mmb(interact -> {
                                                                                        // toggle inclusion
                                                                                        crate.lootByWeight.remove(lootSet);
                                                                                        crate.weightsToSums();
                                                                                        return Result.REFRESH();
                                                                                    }).lmb(interact -> {
                                                                                        // decrement
                                                                                        int change = interact.shift ? 5 : 1;

                                                                                        crate.lootByWeight.put(lootSet, Util.clamp(weight - change, 1, Integer.MAX_VALUE));
                                                                                        crate.weightsToSums();

                                                                                        return Result.REFRESH();
                                                                                    }).rmb(interact -> {
                                                                                        // decrement
                                                                                        int change = interact.shift ? 5 : 1;

                                                                                        crate.lootByWeight.put(lootSet, Util.clamp(weight + change, 1, Integer.MAX_VALUE));
                                                                                        crate.weightsToSums();

                                                                                        return Result.REFRESH();
                                                                                    });
                                                                                } else {
                                                                                    b.lore("&fMMB: &7" + L(p, "toggle", "toggle"));
                                                                                    btn.mmb(interact -> {
                                                                                        crate.lootByWeight.put(lootSet, 1);
                                                                                        crate.weightsToSums();
                                                                                        return Result.REFRESH();
                                                                                    });
                                                                                }
                                                                                result1.add(btn.icon(b::build).get());
                                                                            }

                                                                            return result1;
                                                                        })
                                                                )
                                                                // *   *   *
                                                                // Edit Columns
                                                                // *   *   *
                                                                .button(7, 1, new Button.Builder()
                                                                        .icon(() -> ItemBuilder.copyOf(Material.LADDER).name("&8&n" + L(p, "Columns", "Columns") + "&r&8: &7" + crate.columns).lore(LORE_LMB_NUM + "\n" + LORE_RMB_NUM).amount(crate.columns).build())
                                                                        .lmb(interact -> {
                                                                            // decrease
                                                                            crate.columns = Util.clamp(crate.columns - 1, 1, 6);
                                                                            return Result.REFRESH();
                                                                        })
                                                                        .rmb(interact -> {
                                                                            // decrease
                                                                            crate.columns = Util.clamp(crate.columns + 1, 1, 6);
                                                                            return Result.REFRESH();
                                                                        }))
                                                                // *   *   *
                                                                // Edit Picks
                                                                // *   *   *
                                                                .button(2, 3, new Button.Builder()
                                                                        .icon(() -> ItemBuilder.copyOf(Material.MELON_SEEDS).name("&8&n" + L(p, "Picks", "Picks") + "&r&8: &7" + crate.picks).lore(LORE_LMB_NUM + "\n" + LORE_RMB_NUM).amount(crate.picks).build())
                                                                        .lmb(interact -> {
                                                                            // decrease
                                                                            crate.picks = Util.clamp(crate.picks - 1, 1, crate.columns*9);
                                                                            return Result.REFRESH();
                                                                        })
                                                                        .rmb(interact -> {
                                                                            // decrease
                                                                            crate.picks = Util.clamp(crate.picks + 1, 1, crate.columns*9);
                                                                            return Result.REFRESH();
                                                                        }))
                                                                // *   *   *
                                                                // Edit Pick Sound
                                                                // *   *   *
                                                                .childButton(6, 3, () -> ItemBuilder.copyOf(Material.JUKEBOX).name("&a&n" + L(p, "Sound", "Sound") + "&r&a: &r&7" + crate.sound).lore(L(p, "LORE_LMB_EDIT", LORE_LMB_EDIT)).build(),
                                                                        new TextMenu.TBuilder()
                                                                                .title(L(p, "sound", "sound"), true)
                                                                                .leftRaw(() -> LOREM_IPSUM)
                                                                                .right(() -> L(p, "Input_a_sound", "Input a sound"))
                                                                                .onClose((player, reroute) -> Result.BACK())
                                                                                .onComplete((player, s) -> {
                                                                                    try {
                                                                                        Sound sound = Sound.valueOf(s.toUpperCase());
                                                                                        crate.sound = sound;
                                                                                        p.playSound(p.getLocation(), sound, 1, 1);
                                                                                        return Result.BACK();
                                                                                    } catch (Exception e) {
                                                                                        return Result.TEXT(L(p, "Invalid_sound", "Invalid sound"));
                                                                                    }
                                                                                })
                                                                ),
                                                        /// RMB - delete crate
                                                        interact -> {
                                                            Main.get().data.crates.remove(crate.id);
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
                ).childButton(3, 1, () -> ItemBuilder.of("EXPERIENCE_BOTTLE").name("&6&l" + L(p, "LOOT", "Loot")).build(), new ParallaxMenu.PBuilder()
                        .title(L(p, "LOOTSETS", "lootSets"), true)
                        .parentButton(4, 5)
                        .addAll(self -> {
                            ArrayList<Button> result = new ArrayList<>();
                            for (LootSet lootSet : Main.get().data.lootSets.values()) {
                                /*
                                 * List all LootSets
                                 */
                                result.add(new Button.Builder()
                                        .icon(() -> ItemBuilder.copyOf(lootSet.itemStack).lore("&8id: " + lootSet.id + "\n" + "&8" + lootSet.loot.size() + " " + L(p, "elements", "elements") + "\n" + L(p, "LORE_LMB_EDIT", LORE_LMB_EDIT) + "\n" + L(p, "LORE_RMB_DEL", LORE_RMB_DEL)).build())
                                        .child(self, new ParallaxMenu.PBuilder()
                                                        .title(lootSet.id, true)
                                                        .parentButton(4, 5)
                                                        .addAll(self1 -> {
                                                            ArrayList<Button> result1 = new ArrayList<>();
                                                            for (ILoot a : lootSet.loot) {
                                                                ItemStack copy = a.getIcon(null);

                                                                result1.add(new Button.Builder()
                                                                        .icon(() -> ItemBuilder.copyOf(copy).lore(a + "\n" + L(p, "LORE_LMB_EDIT", LORE_LMB_EDIT) + "\n" + L(p, "LORE_RMB_DEL", LORE_RMB_DEL)).build())
                                                                        .child(self1, a.getMenuBuilder(), interact -> {
                                                                            if (lootSet.loot.size() > 1) {
                                                                                // delete
                                                                                lootSet.loot.remove(a);
                                                                                return Result.REFRESH();
                                                                            }
                                                                            return null;
                                                                        })
                                                                        .get());
                                                            }
                                                            return result1;
                                                        })
                                                        .childButton(3, 5, () -> ItemBuilder.copyOf(lootSet.itemStack).name(L(p, "NAME_EDIT", NAME_EDIT)).build(), new ItemMutateMenuBuilder()
                                                                .build(lootSet.itemStack, itemStack -> lootSet.itemStack = itemStack))
                                                        .childButton(5, 5, ITEM_NEW::build, new ParallaxMenu.PBuilder()
                                                                .title(L(p, "new_loot", "new loot"), true)
                                                                //.onClose(player -> EnumResult.BACK)
                                                                .parentButton(4, 5)
                                                                .addAll(self1 -> {
                                                                    ArrayList<Button> result1 = new ArrayList<>();
                                                                    for (Map.Entry<Class<? extends ILoot>, ItemStack> entry
                                                                            : LootCratesAPI.lootClasses.entrySet()) {
                                                                        //AbstractLoot aLootInstance = new a
                                                                        result1.add(new Button.Builder()
                                                                                // This causes a nullptr because it is instantly constructed?
                                                                                //.icon(() -> ItemBuilder.copyOf(Material.GOLD_INGOT).name(menuClazz.getSimpleName()).build())
                                                                                //.child(self1.parentMenuBuilder, lootSet.addLoot(
                                                                                //        (ILoot) ReflectionUtil.invokeConstructor(menuClazz)).getMenuBuilder())

                                                                                .icon(entry::getValue)
                                                                                .lmb(interact -> {
                                                                                    AbstractMenu.Builder menu = lootSet.addLoot(
                                                                                            (ILoot) ReflectionUtil.invokeConstructor(entry.getKey())).getMenuBuilder();
                                                                                    menu.parent(self1.parentMenuBuilder);
                                                                                    return Result.OPEN(menu);
                                                                                })
                                                                                .get());
                                                                    }
                                                                    return result1;
                                                                })
                                                        ),
                                                // RMB - delete lootSet
                                                interact -> {
                                                    if (Main.get().data.lootSets.size() > 1) {
                                                        Main.get().data.lootSets.remove(lootSet.id);
                                                        for (Crate crate : Main.get().data.crates.values()) {
                                                            Integer removed = crate.lootByWeight.remove(lootSet);
                                                            if (removed != null)
                                                                crate.weightsToSums();
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
                        .childButton(5, 5, ITEM_NEW::build, new TextMenu.TBuilder()
                                .title(L(p, "new_lootSet", "new lootSet"), true)
                                .leftRaw(() -> LOREM_IPSUM)
                                .onClose((player, reroute) -> !reroute ? Result.BACK() : null)
                                .onComplete((player, text) -> {
                                    if (!text.isEmpty() && !Main.get().data.lootSets.containsKey(text)) {
                                        // Java 16
                                        //Main.get().data.lootSets.put(text,
                                        //        new LootSet(text, new ItemStack(Material.RED_STAINED_GLASS),
                                        //                new ArrayList<>(List.of(new LootItem()))));
                                        Main.get().data.lootSets.put(text,
                                                new LootSet(text, new ItemStack(Material.GLOWSTONE_DUST),
                                                        new ArrayList<>(Collections.singletonList(new LootItem()))));

                                    }
                                    return Result.BACK();
                                })
                        )
                )
                /*
                 * Global Fireworks Edit
                 */
                .childButton(5, 1, () -> ItemBuilder.of("FIREWORK_ROCKET").name("&e&l" + L(p, "Fireworks", "Fireworks")).build(), new SimpleMenu.SBuilder(5)
                        .title(L(p, "firework", "firework"), true)
                        .background()
                        .button(4, 0, IN_OUTLINE)
                        .button(3, 1, IN_OUTLINE)
                        .button(5, 1, IN_OUTLINE)
                        .button(4, 2, IN_OUTLINE)
                        .button(1, 1, new Button.Builder()
                                .icon(() -> ItemBuilder.of("FIREWORK_STAR").fireworkEffect(Main.get().data.fireworkEffect).build()))
                        .button(4, 1, new Button.Builder()
                                .icon(() -> ItemBuilder.of("FIREWORK_STAR").fireworkEffect(Main.get().data.fireworkEffect).build())
                                .lmb(interact -> {
                                    if (interact.heldItem != null) {
                                        //if (interact.heldItem.getItemMeta() instanceof FireworkEffectMeta meta && meta.hasEffect()) {
                                        if (interact.heldItem.getItemMeta() instanceof FireworkEffectMeta) {
                                            FireworkEffectMeta meta = (FireworkEffectMeta) interact.heldItem.getItemMeta();
                                            if (meta.hasEffect()) {
                                                Main.get().data.fireworkEffect = meta.getEffect();
                                                return Result.REFRESH();
                                            }
                                        }
                                        interact.player.sendMessage(ChatColor.YELLOW + L(p, "must_have_effect", "must have effect"));
                                    }

                                    return Result.GRAB();
                                }))
                        .parentButton(4, 4)
                )
                /*
                 * Misc settings
                 */
                .childButton(7, 1, () -> ItemBuilder.of("IRON_HORSE_ARMOR").name("&8&l" + L(p, "Misc", "Misc")).build(), new SimpleMenu.SBuilder(5)
                        .title(L(p, "misc", "misc"), true)
                        .button(1, 1, new Button.Builder()
                                .icon(() -> ItemBuilder.of("COMPARATOR").name("&e&l" + L(p, "Toggle_Debug", "Toggle Debug")).lore(Main.get().data.debug ? "&2" + L(p, "enabled", "enabled") : "&c" + L(p, "disabled", "disabled")).build())
                                .lmb(interact -> { Main.get().data.debug ^= true; return Result.REFRESH(); }))
                        .button(3, 1, new Button.Builder()
                                .icon(() -> ItemBuilder.copyOf(Material.PAINTING).name("&6&l" + L(p, "Toggle_Lang", "Toggle translations")).lore(Main.get().data.debug ? "&2" + L(p, "enabled", "enabled") : "&c" + L(p, "disabled", "disabled")).build())
                                .lmb(interact -> { Main.get().data.lang ^= true; return Result.REFRESH(); }))
                        .parentButton(4, 4)
                )
                /*
                 * Finally open
                 */
                .open(p);

        // en_us
        //
        //p.getLocale()

    }

}
