package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ui.*;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import com.crazicrafter1.lootcrates.crate.LootCollection;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Editor {

    public static final String LOREM_IPSUM = "Lorem ipsum";
    private static final String COLORS = ColorUtil.renderMarkers("&a" + LOREM_IPSUM) +                                  "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "&a" + LOREM_IPSUM +                                    "\n"
            +   ColorUtil.renderMarkers("&#456789" + LOREM_IPSUM) +                                                     "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "&#456789" + LOREM_IPSUM +                              "\n"
            +   ColorUtil.renderAll("<#aa7744>" + LOREM_IPSUM + "</#abcdef>") +                                         "\n" + ChatColor.WHITE + "   : " + ChatColor.GRAY + "<#aa7744>" + LOREM_IPSUM + "</#abcdef>" +              "\n"
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
                                                .child(self, crate.getBuilder())
                                                .rmb(
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
                                        .child(self, lootSet.getBuilder()) // LMB - Edit Loot Collection
                                        .rmb(
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
                        // Type
                        .button(0, 0, new Button.Builder().icon(p -> {
                            FireworkEffect effect = LCMain.get().rewardSettings.fireworkEffect;

                            String base64;
                            switch (effect.getType()) {
                                case BALL: base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDcxMWU1NDcyYzU3YzMyMTgwOGI3YmUzNDRhMTFlZmFhNGRlYjViNDA0NTU2OTdlZDRhM2U2ZTkyODc3MjAwMiJ9fX0="; break;
                                case STAR: base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTIzODJlZWVhZWNjMzM5Y2ZhZjgzYjRiMTk2ZTVlMDAwZTdiNmZlNmM4MWZjZTNjYzNjOGFlM2VkMWMwNDNkNCJ9fX0="; break;
                                case BURST: base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGExYWY3YjIxZTljM2EzYzdhNGExNGZkM2RmYzhkZjgxYmU2OWY0ODkwYzVjOWE3YjUxYTYwYWU2NDQ0OGQ1NCJ9fX0="; break;
                                case CREEPER: base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQyYmVhNzQ0NThjNWM1YjQ5Y2RmODMyYmUwNTI3YTA0ZTcyYjRlNzMzZmQ4NWEwOTE5MjBjNWY1NGJlN2FlYiJ9fX0="; break;
                                default: base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzJlYmVhMTdjMzIzNTYzN2E3NDQ4ODczODA2MDllMzhmYWU3NDhhMjY5YzY3NThkZDA5Njk4NmYyYWI5ZjgxNCJ9fX0="; break;
                            }

                            return ItemBuilder.from("PLAYER_HEAD").skull(base64)
                                    .name(String.format(Lang.ED_Fireworks_Type, WordUtils.capitalize(effect.getType().name().toLowerCase())))
                                    .lore(Arrays.stream(FireworkEffect.Type.values()).map(type -> "&7" + (effect.getType() == type ? "&l" : "") + WordUtils.capitalize(type.name().toLowerCase())).collect(Collectors.toList())).build();
                        }).click(e -> {
                            ClickType clickType = e.clickType;
                            if (!(clickType.isRightClick() || clickType.isLeftClick()))
                                return Result.ok();

                            RewardSettings settings = LCMain.get().rewardSettings;
                            FireworkEffect effect = settings.fireworkEffect;

                            FireworkEffect.Type[] values = FireworkEffect.Type.values();
                            int index = (Arrays.asList(values).indexOf(effect.getType()) + (clickType.isLeftClick() ? -1 : 1)) % values.length;
                            if (index < 0) index += values.length;
                            FireworkEffect.Type nextType = values[index];

                            //settings.fireworkEffect = new FireworkEffect(effect.hasFlicker(), effect.hasTrail(), effect.getColors(), effect.getFadeColors(), nextType);

                            settings.fireworkEffect = FireworkEffect.builder()
                                    .with(nextType)
                                    .flicker(effect.hasFlicker())
                                    .trail(effect.hasTrail())
                                    .withColor(effect.getColors())
                                    .withFade(effect.getFadeColors()).build();

                            return Result.refresh();
                        }))
                        // Flicker
                        .button(2, 0, new Button.Builder().icon(p40 -> ItemBuilder.from("PLAYER_HEAD")
                                .skull(LCMain.get().rewardSettings.fireworkEffect.hasFlicker() ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM4YWZiZmUzZmJkYmRkNTRlZTkxYWZlYTkxYTczY2ZjNjY2MzUyYzI3ZTcwNmYyYzM5MjE0MGY3MjAzMTI4YSJ9fX0=" : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTBhZjMyMzhmNjNhYjIwYzU5YjE1OGY0MDQ3YmViNTVkYjExNmQxYTk0OThhZWE0YjlhZTU4MTk5MGZmOGQxNyJ9fX0=")
                                .name((LCMain.get().rewardSettings.fireworkEffect.hasFlicker() ? "&6&l" : "&7") + "Flicker (Click to toggle)")
                                .build())
                            .lmb(e -> {
                                // toggle
                                FireworkEffect effect = LCMain.get().rewardSettings.fireworkEffect;
                                LCMain.get().rewardSettings.fireworkEffect = FireworkEffect.builder()
                                        .with(effect.getType())
                                        .flicker(!effect.hasFlicker())
                                        .trail(effect.hasTrail())
                                        .withColor(effect.getColors())
                                        .withFade(effect.getFadeColors()).build();
                                return Result.refresh();
                            })
                        )
                        // Trail
                        .button(7, 0, new Button.Builder().icon(p40 -> ItemBuilder.from("PLAYER_HEAD")
                                        .skull(LCMain.get().rewardSettings.fireworkEffect.hasTrail() ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQzMGM1YjkzNTgxNzlkMDk4Nzc0MGQ3NDc4YzBlZWI2YjljN2ZhMDdjZTQ4OGRkNjk4NTE4MWFmNjFmYjhhMiJ9fX0=" : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg3ZDgzNWI1NDNlZDFiMDI0MTU3MDFjYTdiM2Y4YzhhMGExMTJhZjEzMThmOWNlYzVhNWU5MWU0ODE0YTI0OSJ9fX0=")
                                        .name((LCMain.get().rewardSettings.fireworkEffect.hasTrail() ? "&c&l" : "&7") + "Trail (Click to toggle)")
                                        .build())
                                .lmb(e -> {
                                    // toggle
                                    FireworkEffect effect = LCMain.get().rewardSettings.fireworkEffect;
                                    LCMain.get().rewardSettings.fireworkEffect = FireworkEffect.builder()
                                            .with(effect.getType())
                                            .flicker(effect.hasFlicker())
                                            .trail(!effect.hasTrail())
                                            .withColor(effect.getColors())
                                            .withFade(effect.getFadeColors()).build();
                                    return Result.refresh();
                                })
                        )
                        // TODO use the same code for colors as fade colors
                        //  just change titles and references between colors <-> fade ...
                        .childButton(0, 2, p0010 -> ItemBuilder.from("FIREWORK_STAR").name("&2Colors").build(), new ListMenu.LBuilder()
                                .title(p0 -> "Colors editor")
                                .background()
                                .parentButton(4, 5)
                                .addAll((self, p) -> {
                                    RewardSettings settings = LCMain.get().rewardSettings;

                                    return Streams.mapWithIndex(settings.fireworkEffect.getColors().stream(), (color, colorIndex) -> new Button.Builder()
                                        .child(self, new TextMenu.TBuilder()
                                                .title(p049 -> "Set color")
                                                .leftRaw(p00202 -> "#" + Integer.toHexString(color.asRGB()))
                                                .parentOnClose()
                                                .onComplete((p0020, text, menu) -> {
                                                    int value;

                                                    try {
                                                        int i = text.indexOf("0x");
                                                        if (i != -1)
                                                            value = Integer.parseUnsignedInt(text, i + 2, text.length(), 16);
                                                        else {
                                                            i = text.indexOf("#");
                                                            if (i != -1)
                                                                value = Integer.parseUnsignedInt(text, i + 1, text.length(), 16);
                                                            else {
                                                                // decimal
                                                                value = Integer.parseUnsignedInt(text);
                                                            }
                                                        }
                                                    } catch (Exception ignored) {
                                                        return Result.text("Must match: 1234567, #abcdef, 0xabcdef, ...");
                                                    }

                                                    if ((value > 0xFFFFFF) || value < 0)
                                                        return Result.text("Too large");

                                                    FireworkEffect effect = settings.fireworkEffect;

                                                    List<Color> colors = new ArrayList<>(effect.getColors());

                                                    Color color1 = Color.fromRGB(value);
                                                    if (colors.contains(color1))
                                                        return Result.text("Color already applied");

                                                    colors.set((int) colorIndex, color1);

                                                    settings.fireworkEffect = FireworkEffect.builder()
                                                            .with(effect.getType())
                                                            .flicker(effect.hasFlicker())
                                                            .trail(effect.hasTrail())
                                                            .withColor(colors) // Colors are reassigned with the copied list
                                                            .withFade(effect.getFadeColors()).build();

                                                    return Result.parent();
                                                })
                                        )
                                        .icon(p102 -> ItemBuilder.copy(Material.LEATHER_CHESTPLATE)
                                                .name(ColorUtil.toHexMarker(color) + "0x" + Integer.toHexString(color.asRGB()))
                                                .color(color)
                                                .build()
                                        )
                                        .get()
                                ).collect(Collectors.toList());})
                        )
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
                                                //meta.getEffect().getType() == FireworkEffect.Type.BALL;
                                                FireworkEffect effect = meta.getEffect();
                                                //effect.getColors().get(0)
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
