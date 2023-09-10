package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ui.*;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FireworkModifyMenu extends SimpleMenu.SBuilder {

    // Helper method to prevent code duplication across colors and fade colors
    private void addColorButton(int x, int y,
                                String nameFormat,
                                Function<Player, String> title,
                                Function<FireworkEffect, List<Color>> colorFunction,
                                BiFunction<FireworkEffect.Builder, List<Color>, FireworkEffect.Builder> colorApplier)
    {
        final RewardSettings settings = LCMain.get().rewardSettings;

        childButton(x, y,
                // Menu Icon
                p0010 -> {
                    FireworkEffect effect = settings.fireworkEffect;
                    List<Color> constColors = colorFunction.apply(effect);
                    return ItemBuilder.from("FIREWORK_STAR")
                            .name(String.format(nameFormat, constColors.size()))
                            .lore(constColors.stream().map(color -> String.format("&7 - #%06X %s\u2588", color.asRGB(), ColorUtil.toHexMarker(color))).collect(Collectors.toList())).build();
                },
                new ListMenu.LBuilder()
                        .title(title)
                        .background()
                        .parentButton(4, 5)
                        // Add color
                        .childButton(5, 5, p -> ItemBuilder.copy(Material.NAME_TAG).name(Lang.EDITOR_FIREWORK_ADD).build(), new TextMenu.TBuilder()
                                .title(p -> Lang.EDITOR_FIREWORK_COLOR_NEW)
                                .leftRaw(p -> Lang.EDITOR_FIREWORK_COLOR_HINT)
                                // TODO this code is 99% similar to the color editor
                                .onComplete((p, text, self) -> {
                                    int value;

                                    try {
                                        int i = text.indexOf('x');
                                        if (i != -1)
                                            value = Integer.parseUnsignedInt(text, i + 1, text.length(), 16);
                                        else {
                                            i = text.indexOf('#');
                                            if (i != -1) {
                                                if (text.length() != 7)
                                                    return Result.text(Lang.EDITOR_FIREWORK_ERROR1);
                                                value = Integer.parseUnsignedInt(text, i + 1, text.length(), 16);
                                            } else {
                                                // decimal
                                                value = Integer.parseUnsignedInt(text);
                                            }
                                        }
                                    } catch (Exception ignored) {
                                        return Result.text(Lang.EDITOR_FIREWORK_ERROR3);
                                    }

                                    if (value > 0xFFFFFF)
                                        return Result.text(Lang.EDITOR_FIREWORK_ERROR2);

                                    List<Color> constColors = colorFunction.apply(settings.fireworkEffect);

                                    Color color1 = Color.fromRGB(value);
                                    if (constColors.contains(color1))
                                        return Result.text(Lang.EDITOR_FIREWORK_ERROR0);

                                    List<Color> colors = new ArrayList<>(constColors);
                                    colors.add(color1);

                                    FireworkEffect effect = settings.fireworkEffect;

                                    settings.fireworkEffect = colorApplier.apply(FireworkEffect.builder()
                                            .with(effect.getType())
                                            .flicker(effect.hasFlicker())
                                            .trail(effect.hasTrail()), colors).build();

                                    return Result.parent();
                                })
                        )
                        // Edit colors
                        .addAll((self, p) -> {
                            List<Color> constColors = colorFunction.apply(settings.fireworkEffect);

                            return Streams.mapWithIndex(constColors.stream(), (color, colorIndex) -> new Button.Builder()
                                    // Remove color
                                    .bind(ClickType.SHIFT_RIGHT, e -> {
                                        if (constColors.size() > 1) {
                                            // cancel
                                            List<Color> colors = new ArrayList<>(constColors);
                                            colors.remove((int)colorIndex); // will shift elements

                                            FireworkEffect effect = settings.fireworkEffect;

                                            settings.fireworkEffect = colorApplier.apply(FireworkEffect.builder()
                                                    .with(effect.getType())
                                                    .flicker(effect.hasFlicker())
                                                    .trail(effect.hasTrail()), colors).build();
                                            return Result.refresh();
                                        }
                                        return Result.message(Lang.EDITOR_FIREWORK_ERROR4);
                                    })
                                    .icon(p102 -> ItemBuilder.copy(Material.LEATHER_CHESTPLATE)
                                            .name(String.format("&7#%06X %s\u2588", color.asRGB(), ColorUtil.toHexMarker(color)))
                                            .lore(Lang.EDITOR_DELETE)
                                            .color(color)
                                            .hideFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES)
                                            .build()
                                    )
                                    .get()
                            ).collect(Collectors.toList());
                        })
        );
    }

    public FireworkModifyMenu() {
        super(3);

        final RewardSettings settings = LCMain.get().rewardSettings;

        this.title(p -> Lang.EDITOR_FIREWORK_TITLE)
                .background()
                // help
                .button(8, 2, new Button.Builder().icon(p -> ItemBuilder.copy(Material.PAPER).name("&aToggle/change settings with LMB").build()))
                // Type
                .button(1, 1, new Button.Builder().icon(p -> {
                    FireworkEffect effect = settings.fireworkEffect;

                    String base64;
                    switch (effect.getType()) {
                        case BALL: base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDcxMWU1NDcyYzU3YzMyMTgwOGI3YmUzNDRhMTFlZmFhNGRlYjViNDA0NTU2OTdlZDRhM2U2ZTkyODc3MjAwMiJ9fX0="; break;
                        case STAR: base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTIzODJlZWVhZWNjMzM5Y2ZhZjgzYjRiMTk2ZTVlMDAwZTdiNmZlNmM4MWZjZTNjYzNjOGFlM2VkMWMwNDNkNCJ9fX0="; break;
                        case BURST: base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGExYWY3YjIxZTljM2EzYzdhNGExNGZkM2RmYzhkZjgxYmU2OWY0ODkwYzVjOWE3YjUxYTYwYWU2NDQ0OGQ1NCJ9fX0="; break;
                        case CREEPER: base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQyYmVhNzQ0NThjNWM1YjQ5Y2RmODMyYmUwNTI3YTA0ZTcyYjRlNzMzZmQ4NWEwOTE5MjBjNWY1NGJlN2FlYiJ9fX0="; break;
                        default: base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzJlYmVhMTdjMzIzNTYzN2E3NDQ4ODczODA2MDllMzhmYWU3NDhhMjY5YzY3NThkZDA5Njk4NmYyYWI5ZjgxNCJ9fX0="; break;
                    }

                    return ItemBuilder.fromSkull(base64)
                            .name(Lang.EDITOR_FIREWORK_TYPE)
                            .lore(Arrays.stream(FireworkEffect.Type.values()).map(type -> (effect.getType() == type ? "&6" : "&7") + WordUtils.capitalize(type.name().toLowerCase().replace('_', ' '))).collect(Collectors.toList())).build();
                }).bind(e -> {
                    boolean left = e.clickType.isLeftClick();

                    FireworkEffect effect = settings.fireworkEffect;

                    FireworkEffect.Type[] values = FireworkEffect.Type.values();
                    int index = (Arrays.asList(values).indexOf(effect.getType()) + (left ? -1 : 1)) % values.length;
                    if (index < 0) index += values.length;
                    FireworkEffect.Type nextType = values[index];

                    settings.fireworkEffect = FireworkEffect.builder()
                            .with(nextType)
                            .flicker(effect.hasFlicker())
                            .trail(effect.hasTrail())
                            .withColor(effect.getColors())
                            .withFade(effect.getFadeColors()).build();

                    return Result.refresh();
                }, ClickType.RIGHT, ClickType.LEFT))
                // Flicker
                .button(3, 1, new Button.Builder().icon(p40 -> ItemBuilder
                                .fromSkull(settings.fireworkEffect.hasFlicker() ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM4YWZiZmUzZmJkYmRkNTRlZTkxYWZlYTkxYTczY2ZjNjY2MzUyYzI3ZTcwNmYyYzM5MjE0MGY3MjAzMTI4YSJ9fX0=" : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTBhZjMyMzhmNjNhYjIwYzU5YjE1OGY0MDQ3YmViNTVkYjExNmQxYTk0OThhZWE0YjlhZTU4MTk5MGZmOGQxNyJ9fX0=")
                                .name("&7Flicker (" + (settings.fireworkEffect.hasFlicker() ? "&aon" : "&coff") + "&7)")
                                .build())
                        .lmb(e -> {
                            // toggle
                            FireworkEffect effect = settings.fireworkEffect;
                            settings.fireworkEffect = FireworkEffect.builder()
                                    .with(effect.getType())
                                    .flicker(!effect.hasFlicker())
                                    .trail(effect.hasTrail())
                                    .withColor(effect.getColors())
                                    .withFade(effect.getFadeColors()).build();
                            return Result.refresh();
                        })
                )
                // Trail
                .button(4, 1, new Button.Builder().icon(p40 -> ItemBuilder
                                .fromSkull(settings.fireworkEffect.hasTrail() ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQzMGM1YjkzNTgxNzlkMDk4Nzc0MGQ3NDc4YzBlZWI2YjljN2ZhMDdjZTQ4OGRkNjk4NTE4MWFmNjFmYjhhMiJ9fX0=" : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg3ZDgzNWI1NDNlZDFiMDI0MTU3MDFjYTdiM2Y4YzhhMGExMTJhZjEzMThmOWNlYzVhNWU5MWU0ODE0YTI0OSJ9fX0=")
                                .name("&7Trail (" + (settings.fireworkEffect.hasTrail() ? "&aon" : "&coff") + "&r&7)")
                                .build())
                        .lmb(e -> {
                            // toggle
                            FireworkEffect effect = settings.fireworkEffect;
                            settings.fireworkEffect = FireworkEffect.builder()
                                    .with(effect.getType())
                                    .flicker(effect.hasFlicker())
                                    .trail(!effect.hasTrail())
                                    .withColor(effect.getColors())
                                    .withFade(effect.getFadeColors()).build();
                            return Result.refresh();
                        })
                )
                // TODO add button to test out firework
                //  either fire it or give it to the player?

                // TODO we'll just ignore this mess unless someone wants it
                // apply firework from item
                /*
                .capture(new Button.Builder().lmb(e -> {
                    if (e.clickType.isLeftClick() || e.clickType.isRightClick()) {
                        // set firework to inputted firework
                        if (e.heldItem != null) {
                            ItemMeta meta = e.heldItem.getItemMeta();
                            FireworkEffectMeta fmeta = meta instanceof FireworkMeta ? ((FireworkMeta) meta). : meta instanceof FireworkEffectMeta ? (FireworkEffectMeta) meta : null;
                            if (e.heldItem.getItemMeta() instanceof FireworkEffectMeta) {
                                FireworkEffectMeta meta = (FireworkEffectMeta) e.heldItem.getItemMeta();
                                if (meta.hasEffect()) {
                                    settings.fireworkEffect = meta.getEffect();
                                    return Result.message("Applied firework").andThen(Result.refresh());
                                }
                            }
                            return Result.message(Lang.ED_Firework_ERROR);
                        }
                    }

                    return Result.ok();
                }))*/
                .parentButton(4, 2);

        // Add basic color button
        this.addColorButton(6, 1, Lang.EDITOR_FIREWORK_NAME_COLORS,
                p0 -> Lang.EDITOR_FIREWORK_TITLE_COLORS, FireworkEffect::getColors,
                (fb, colors) -> fb.withColor(colors).withFade(settings.fireworkEffect.getFadeColors()));

        // Add fade color button
        this.addColorButton(7, 1, Lang.EDITOR_FIREWORK_NAME_FADE,
                p0 -> Lang.EDITOR_FIREWORK_TITLE_FADE, FireworkEffect::getFadeColors,
                (fb, colors) -> fb.withColor(settings.fireworkEffect.getColors()).withFade(colors));
    }

}
