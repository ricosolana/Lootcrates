package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Editor {

    private final Player p;

    //public static final Button.Builder inOutline = new Button.Builder().icon(() -> ItemBuilder.copyOf(Material.GRAY_STAINED_GLASS_PANE).name("&7Set to").toItem());

    public static final String LMB_Dec = Lang.A.LMB + " &c-";
    public static final String RMB_Inc = Lang.A.RMB + " &a+";

    /// TODO make this butchery mess somehow translatable
    public static String ColorDem = "&7'&' " + (Version.AT_LEAST_v1_16.a() ?
            "or '#&': &#2367fbc&#3f83fbo&#5a9ffcl&#76bbfco&#91d7fcr&#acf2fds" : ": &fcolors") +
            "\n&7Macros: &6%lc_picks%&7, &6%lc_id%\n&7Supports PlaceholderAPI";

    public static final Button.Builder IN_OUTLINE = new Button.Builder().icon(p -> ItemBuilder.of(
            "GRAY_STAINED_GLASS_PANE").name("&7" + Lang.L(p, Lang.A.Set_to)).build());

    public static final String BASE64_DEC = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM1YThhYThhNGMwMzYwMGEyYjVhNGViNmJlYjUxZDU5MDI2MGIwOTVlZTFjZGFhOTc2YjA5YmRmZTU2NjFjNiJ9fX0=";
    public static final String BASE64_INC = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFiOTVhODc1MWFlYWEzYzY3MWE4ZTkwYjgzZGU3NmEwMjA0ZjFiZTY1NzUyYWMzMWJlMmY5OGZlYjY0YmY3ZiJ9fX0=";

    public static final Pattern TRANSLATION_STRIPPER_PATTERN = Pattern.compile("([!-/:-@\\[-^`{-~])+");
    public static final Pattern VALID_KEY_PATTERN = Pattern.compile("(?=.*[a-z])[a-z_]+");

    private String L(String keyMsg) {
        return Lang.L(p, keyMsg);
    }

    public Editor(Player p) {
        this.p = p;
    }

    public void open() {
        new SimpleMenu.SBuilder(3)
                .title(p -> L(Lang.A.Editor))
                .background()
                /* *************** *\
                *                   *
                * Global Crate List *
                *                   *
                \* *************** */
                .childButton(1, 1, p -> ItemBuilder.copyOf(Material.CHEST).name("&3&l" + L(Lang.A.Crates)).build(), new ParallaxMenu.PBuilder()
                                .title(p -> L(Lang.A.Crates))
                                .parentButton(4, 5)
                                // *       *      *
                                // Add Crate button
                                // *       *      *
                                .childButton(5, 5, p -> ItemBuilder.copyOf(Material.NETHER_STAR).name("&6" + L(Lang.A.New)).build(), new TextMenu.TBuilder()
                                        .title(p -> L(Lang.A.New_crate))
                                        .leftRaw(p -> L(Lang.A.Lorem_ipsum))
                                        .right(p -> L(Lang.A.Format_strict), p -> "&7 - " + L(Lang.A.Format_strict1) + "\n&7 - " + L(Lang.A.Format_strict2))
                                        .onClose((player) -> Result.PARENT())
                                        .onComplete((player, s, b) -> {
                                            if (!VALID_KEY_PATTERN.matcher(s).matches() || Main.get().data.crates.containsKey(s))
                                                return Result.TEXT(L(Lang.A.Duplicate));

                                            Crate crate = new Crate(s, ItemBuilder.copyOf(Material.ENDER_CHEST).name("my new crate").build(), "select loot", 3, 4, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);

                                            crate.lootBySum = new LinkedHashMap<>();
                                            crate.lootBySum.put(Main.get().data.lootSets.values().iterator().next(), 1);
                                            crate.sumsToWeights();

                                            Main.get().data.crates.put(s, crate);

                                            return Result.PARENT();
                                        })
                                )
                                .addAll(self -> {
                                    ArrayList<Button> result = new ArrayList<>();
                                    for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {
                                        Crate crate = entry.getValue();
                                        result.add(new Button.Builder()
                                                // https://regexr.com/6fdsi
                                                .icon(p -> ItemBuilder.copyOf(crate.itemStack).lore("&8id: " + crate.id + "\n&7" + L(Lang.A.LMB) + ": &a" + L(Lang.A.Edit) + "\n&7" + L(Lang.A.RMB) + ": &c" + L(Lang.A.Delete)).build())
                                                .child(self, new SimpleMenu.SBuilder(5)
                                                                .title(p -> crate.id)
                                                                .background()
                                                                .parentButton(4, 4)
                                                                // *   *   *
                                                                // Edit Crate ItemStack
                                                                // *   *   *
                                                                .childButton(1, 1, p -> ItemBuilder.copyOf(crate.itemStack.getType()).name("&8&n" + L(Lang.A.ItemStack)).lore("&7" + L(Lang.A.LMB) + ": &a" + L(Lang.A.Edit)).build(), new ItemModifyMenu()
                                                                        .build(crate.itemStack.clone(), (itemStack -> {
                                                                            crate.itemStack = itemStack;
                                                                        }))
                                                                )
                                                                // Edit Inventory Title
                                                                .childButton(3, 1, p -> ItemBuilder.copyOf(Material.PAPER).name("&e&n" + L(Lang.A.Title) + "&r&e: " + crate.title).lore("&7" + L(Lang.A.LMB) + ": &a" + L(Lang.A.Edit)).build(), new TextMenu.TBuilder()
                                                                        .title(p -> L(Lang.A.Title))
                                                                        .leftRaw(p -> ColorMode.REVERT.a(crate.title))
                                                                        .onClose((player) -> Result.PARENT())
                                                                        .right(p -> L(ColorDem))
                                                                        .onComplete((player, s, b) -> {
                                                                            if (!s.isEmpty()) {
                                                                                crate.title = ColorMode.COLOR.a("" + s);
                                                                                return Result.PARENT();
                                                                            }

                                                                            return Result.TEXT(L(Lang.A.Invalid));
                                                                        })
                                                                )
                                                                // *   *   *
                                                                // Edit LootSets
                                                                // *   *   *
                                                                .childButton(5, 1, p -> ItemBuilder.of("EXPERIENCE_BOTTLE").name("&6&n" + L(Lang.A.Loot)).lore("&7" + L(Lang.A.LMB) + ": &a" + L(Lang.A.Edit)).build(), new ParallaxMenu.PBuilder()
                                                                        .title(p -> L(Lang.A.Loot))
                                                                        .parentButton(4, 5)
                                                                        .onClose((player) -> Result.PARENT())
                                                                        .addAll(builder -> {
                                                                            ArrayList<Button> result1 = new ArrayList<>();

                                                                            for (LootSet lootSet : Main.get().data.lootSets.values()) {
                                                                                Integer weight = crate.lootByWeight.get(lootSet);
                                                                                Button.Builder btn = new Button.Builder();
                                                                                ItemBuilder b = ItemBuilder.copyOf(lootSet.itemStack.getType()).name("&8" + lootSet.id);
                                                                                if (weight != null) {
                                                                                    b.lore("&7" + crate.getFormattedFraction(lootSet) + "\n" +
                                                                                            "&7" + crate.getFormattedPercent(lootSet) + "\n" +
                                                                                            LMB_Dec + "\n" +
                                                                                            RMB_Inc + "\n" +
                                                                                            "&f" + L(Lang.A.MMB) + ": &7" + L(Lang.A.Toggle) + "\n" +
                                                                                            "&7" + L(Lang.A.SHIFT_Mul) + "&r&7: x5").glow(true);
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
                                                                                    b.lore("&f" + L(Lang.A.MMB) + ": &7" + L(Lang.A.Toggle));
                                                                                    btn.mmb(interact -> {
                                                                                        crate.lootByWeight.put(lootSet, 1);
                                                                                        crate.weightsToSums();
                                                                                        return Result.REFRESH();
                                                                                    });
                                                                                }
                                                                                result1.add(btn.icon(p -> b.build()).get());
                                                                            }

                                                                            return result1;
                                                                        })
                                                                )
                                                                // *   *   *
                                                                // Edit Columns
                                                                // *   *   *
                                                                .button(7, 1, new Button.Builder()
                                                                        .icon(p -> ItemBuilder.copyOf(Material.LADDER).name("&8&n" + L(Lang.A.Columns) + "&r&8: &7" + crate.columns).lore(LMB_Dec + "\n" + RMB_Inc).amount(crate.columns).build())
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
                                                                        .icon(p -> ItemBuilder.copyOf(Material.MELON_SEEDS).name("&8&n" + L(Lang.A.Picks) + "&r&8: &7" + crate.picks).lore(LMB_Dec + "\n" + RMB_Inc).amount(crate.picks).build())
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
                                                                .childButton(6, 3, p -> ItemBuilder.copyOf(Material.JUKEBOX).name("&a&n" + L(Lang.A.Sound) + "&r&a: &r&7" + crate.sound).lore("&7" + L(Lang.A.LMB) + ": &a" + L(Lang.A.Edit)).build(),
                                                                        new TextMenu.TBuilder()
                                                                                .title(p -> L(Lang.A.Sound))
                                                                                .leftRaw(p -> Lang.A.Lorem_ipsum)
                                                                                .right(p -> L(Lang.A.Input_a_sound))
                                                                                .onClose((player) -> Result.PARENT())
                                                                                .onComplete((player, s, b) -> {
                                                                                    try {
                                                                                        Sound sound = Sound.valueOf(s.toUpperCase());
                                                                                        crate.sound = sound;
                                                                                        p.playSound(p.getLocation(), sound, 1, 1);
                                                                                        return Result.PARENT();
                                                                                    } catch (Exception e) {
                                                                                        return Result.TEXT(L(Lang.A.Invalid));
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
                ).childButton(3, 1, p -> ItemBuilder.of("EXPERIENCE_BOTTLE").name("&6&l" + L(Lang.A.LootSets)).build(), new ParallaxMenu.PBuilder()
                        .title(p -> L(Lang.A.LootSets))
                        .parentButton(4, 5)
                        .addAll(self -> {
                            ArrayList<Button> result = new ArrayList<>();
                            for (LootSet lootSet : Main.get().data.lootSets.values()) {
                                /*
                                 * List all LootSets
                                 */
                                result.add(new Button.Builder()
                                        .icon(p -> ItemBuilder.copyOf(lootSet.itemStack).lore("&8id: " + lootSet.id + "\n" + "&8" + lootSet.loot.size() + " " + L(Lang.A.Elements) + "\n&7" + L(Lang.A.LMB) + ": &a" + L(Lang.A.Edit) + "\n&7" + L(Lang.A.RMB) + ": &c" + L(Lang.A.Delete)).build())
                                        .child(self, new ParallaxMenu.PBuilder()
                                                        .title(p -> lootSet.id)
                                                        .parentButton(4, 5)
                                                        .addAll(self1 -> {
                                                            ArrayList<Button> result1 = new ArrayList<>();
                                                            for (ILoot a : lootSet.loot) {
                                                                ItemStack copy = a.getIcon(null);

                                                                result1.add(new Button.Builder()
                                                                        .icon(p -> ItemBuilder.copyOf(copy).lore(a + "\n&7" + L(Lang.A.LMB) + ": &a" + L(Lang.A.Edit) + "\n&7" + L(Lang.A.RMB) + ": &c" + L(Lang.A.Delete)).build())

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
                                                        .childButton(3, 5, p -> ItemBuilder.copyOf(lootSet.itemStack).name(L(Lang.A.Edit)).build(), new ItemModifyMenu()
                                                                .build(lootSet.itemStack, itemStack -> lootSet.itemStack = itemStack))
                                                        .childButton(5, 5, p -> ItemBuilder.copyOf(Material.NETHER_STAR).name("&6" + L(Lang.A.New)).build(), new ParallaxMenu.PBuilder()
                                                                .title(p -> L(Lang.A.New_LootSet))
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
                                                                                //        (ILoot) ReflectionUtil.invokeConstructor(entry.getKey())).getMenuBuilder())

                                                                                .icon(p -> entry.getValue())

                                                                                .lmb(interact -> {
                                                                                    AbstractMenu.Builder menu = lootSet.addLoot(
                                                                                            (ILoot) ReflectionUtil.invokeConstructor(entry.getKey())).getMenuBuilder();

                                                                                    menu.parent(self1.parentMenuBuilder)
                                                                                            .title(p -> menu.getClass().getSimpleName());

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
                        .childButton(5, 5, p -> ItemBuilder.copyOf(Material.NETHER_STAR).name("&6" + L(Lang.A.New)).build(), new TextMenu.TBuilder()
                                .title(p -> L(Lang.A.New_LootSet))
                                .leftRaw(p -> Lang.A.Lorem_ipsum, null, ColorMode.STRIP) // id
                                .onClose((player) -> Result.PARENT())
                                .onComplete((player, s, b) -> {
                                    if (s.isEmpty() || Main.get().data.lootSets.containsKey(s))
                                        return Result.TEXT(L(Lang.A.Duplicate));

                                    Main.get().data.lootSets.put(s,
                                            new LootSet(s, new ItemStack(Material.GLOWSTONE_DUST),
                                                    new ArrayList<>(Collections.singletonList(new LootItem()))));

                                    return Result.PARENT();
                                })
                        )
                )
                /*
                 * Global Fireworks Edit
                 */
                .childButton(5, 1, p -> ItemBuilder.of("FIREWORK_ROCKET").name("&e&l" + L(Lang.A.Firework)).build(), new SimpleMenu.SBuilder(5)
                        .title(p -> L(Lang.A.Firework))
                        .background()
                        .button(4, 0, IN_OUTLINE)
                        .button(3, 1, IN_OUTLINE)
                        .button(5, 1, IN_OUTLINE)
                        .button(4, 2, IN_OUTLINE)
                        .button(1, 1, new Button.Builder()
                                .icon(p -> ItemBuilder.of("FIREWORK_STAR").fireworkEffect(Main.get().data.fireworkEffect).build()))
                        .button(4, 1, new Button.Builder()
                                .icon(p -> ItemBuilder.of("FIREWORK_STAR").fireworkEffect(Main.get().data.fireworkEffect).build())
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
                                        interact.player.sendMessage("&e" + L(Lang.A.Must_have_effect));
                                    }

                                    return Result.GRAB();
                                }))
                        .parentButton(4, 4)
                )
                /*
                 * Misc settings
                 */
                .childButton(7, 1, p -> ItemBuilder.of("IRON_HORSE_ARMOR").name("&8&l" + L(Lang.A.Language)).build(), new SimpleMenu.SBuilder(5)
                        .title(p -> L(Lang.A.Language))
                        .button(1, 1, new Button.Builder()
                                .icon(p -> ItemBuilder.copyOf(Material.PAINTING).name("&6&l" + L(Lang.A.Toggle_translations)).lore(Main.get().lang.translate ? "&2" + L(Lang.A.Enabled) : "&c" + L(Lang.A.Disabled)).build())
                                .lmb(interact -> { Main.get().lang.translate ^= true; return Result.REFRESH(); }))
                        .parentButton(4, 4)
                )
                /*
                 * Finally open
                 */
                .open(p);

    }

}
