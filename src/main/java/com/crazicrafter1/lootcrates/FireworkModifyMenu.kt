package com.crazicrafter1.lootcrates

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.crutils.ui.ListMenu.LBuilder
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.crutils.ui.SimpleMenu.SBuilder
import com.crazicrafter1.crutils.ui.TextMenu.TBuilder
import com.google.common.collect.Streams
import org.apache.commons.lang3.text.WordUtils
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import java.util.stream.Collectors

class FireworkModifyMenu : SBuilder(3) {
    // Helper method to prevent code duplication across colors and fade colors
    private fun addColorButton(x: Int, y: Int,
                               nameFormat: String?,
                               title: Function<Player, String?>,
                               colorFunction: Function<FireworkEffect?, List<Color?>>,
                               colorApplier: BiFunction<FireworkEffect.Builder, List<Color?>, FireworkEffect.Builder>) {
        val settings: RewardSettings = LCMain.Companion.get()!!.rewardSettings
        childButton(x, y,  // Menu Icon
                { p0010: Player? ->
                    val effect = settings.fireworkEffect
                    val constColors = colorFunction.apply(effect)
                    ItemBuilder.from("FIREWORK_STAR")
                            .name(String.format(nameFormat!!, constColors.size))
                            .lore(constColors.stream().map<String> { color: Color? -> String.format("&7 - #%06X %s\u2588", color!!.asRGB(), ColorUtil.toHexMarker(color)) }.collect(Collectors.toList<String>())).build()
                },
                LBuilder()
                        .title(title)
                        .background()
                        .parentButton(4, 5) // Add color
                        .childButton(5, 5, { p: Player? -> ItemBuilder.copy(Material.NAME_TAG).name(Lang.EDITOR_FIREWORK_ADD).build() }, TBuilder()
                                .title { p: Player? -> Lang.EDITOR_FIREWORK_COLOR_NEW }
                                .leftRaw { p: Player? -> Lang.EDITOR_FIREWORK_COLOR_HINT } // TODO this code is 99% similar to the color editor
                                .onComplete { p: Player?, text: String, self: TBuilder? ->
                                    val value: Int
                                    try {
                                        var i = text.indexOf('x')
                                        if (i != -1) value = Integer.parseUnsignedInt(text, i + 1, text.length, 16) else {
                                            i = text.indexOf('#')
                                            value = if (i != -1) {
                                                if (text.length != 7) return@onComplete Result.text(Lang.EDITOR_FIREWORK_ERROR1)
                                                Integer.parseUnsignedInt(text, i + 1, text.length, 16)
                                            } else {
                                                // decimal
                                                Integer.parseUnsignedInt(text)
                                            }
                                        }
                                    } catch (ignored: Exception) {
                                        return@onComplete Result.text(Lang.EDITOR_FIREWORK_ERROR3)
                                    }
                                    if (value > 0xFFFFFF) return@onComplete Result.text(Lang.EDITOR_FIREWORK_ERROR2)
                                    val constColors = colorFunction.apply(settings.fireworkEffect)
                                    val color1 = Color.fromRGB(value)
                                    if (constColors.contains(color1)) return@onComplete Result.text(Lang.EDITOR_FIREWORK_ERROR0)
                                    val colors: MutableList<Color?> = ArrayList(constColors)
                                    colors.add(color1)
                                    val effect = settings.fireworkEffect
                                    settings.fireworkEffect = colorApplier.apply(FireworkEffect.builder()
                                            .with(effect!!.type)
                                            .flicker(effect.hasFlicker())
                                            .trail(effect.hasTrail()), colors).build()
                                    Result.parent()
                                }
                        ) // Edit colors
                        .addAll { self: LBuilder?, p: Player? ->
                            val constColors = colorFunction.apply(settings.fireworkEffect)
                            Streams.mapWithIndex<Color?, Button>(constColors.stream()
                            ) { color: Color?, colorIndex: Long ->
                                Button.Builder() // Remove color
                                        .bind(ClickType.SHIFT_RIGHT) { e: Button.Event? ->
                                            if (constColors.size > 1) {
                                                // cancel
                                                val colors: List<Color?> = ArrayList(constColors)
                                                colors.removeAt(colorIndex.toInt()) // will shift elements
                                                val effect = settings.fireworkEffect
                                                settings.fireworkEffect = colorApplier.apply(FireworkEffect.builder()
                                                        .with(effect!!.type)
                                                        .flicker(effect.hasFlicker())
                                                        .trail(effect.hasTrail()), colors).build()
                                                return@bind Result.refresh()
                                            }
                                            Result.message(Lang.EDITOR_FIREWORK_ERROR4)
                                        }
                                        .icon { p102: Player? ->
                                            ItemBuilder.copy(Material.LEATHER_CHESTPLATE)
                                                    .name(String.format("&7#%06X %s\u2588", color!!.asRGB(), ColorUtil.toHexMarker(color)))
                                                    .lore(Lang.EDITOR_DELETE)
                                                    .color(color)
                                                    .hideFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES)
                                                    .build()
                                        }
                                        .get()
                            }.collect(Collectors.toList<Button>())
                        }
        )
    }

    init {
        val settings: RewardSettings = LCMain.Companion.get()!!.rewardSettings
        this.title { p: Player? -> Lang.EDITOR_FIREWORK_TITLE }
                .background() // help
                .button(8, 2, Button.Builder().icon { p: Player? -> ItemBuilder.copy(Material.PAPER).name("&aToggle/change settings with LMB").build() }) // Type
                .button(1, 1, Button.Builder().icon { p: Player? ->
                    val effect = settings.fireworkEffect
                    val base64: String
                    base64 = when (effect!!.type) {
                        FireworkEffect.Type.BALL -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDcxMWU1NDcyYzU3YzMyMTgwOGI3YmUzNDRhMTFlZmFhNGRlYjViNDA0NTU2OTdlZDRhM2U2ZTkyODc3MjAwMiJ9fX0="
                        FireworkEffect.Type.STAR -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTIzODJlZWVhZWNjMzM5Y2ZhZjgzYjRiMTk2ZTVlMDAwZTdiNmZlNmM4MWZjZTNjYzNjOGFlM2VkMWMwNDNkNCJ9fX0="
                        FireworkEffect.Type.BURST -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGExYWY3YjIxZTljM2EzYzdhNGExNGZkM2RmYzhkZjgxYmU2OWY0ODkwYzVjOWE3YjUxYTYwYWU2NDQ0OGQ1NCJ9fX0="
                        FireworkEffect.Type.CREEPER -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQyYmVhNzQ0NThjNWM1YjQ5Y2RmODMyYmUwNTI3YTA0ZTcyYjRlNzMzZmQ4NWEwOTE5MjBjNWY1NGJlN2FlYiJ9fX0="
                        else -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzJlYmVhMTdjMzIzNTYzN2E3NDQ4ODczODA2MDllMzhmYWU3NDhhMjY5YzY3NThkZDA5Njk4NmYyYWI5ZjgxNCJ9fX0="
                    }
                    ItemBuilder.fromSkull(base64)
                            .name(Lang.EDITOR_FIREWORK_TYPE)
                            .lore(Arrays.stream<FireworkEffect.Type>(FireworkEffect.Type.FireworkEffect.Type.entries.toTypedArray()).map<String> { type: FireworkEffect.Type -> (if (effect.type == type) "&6" else "&7") + WordUtils.capitalize(type.name.lowercase(Locale.getDefault()).replace('_', ' ')) }.collect(Collectors.toList<String>())).build()
                }.bind({ e: Button.Event ->
                    val left = e.clickType.isLeftClick
                    val effect = settings.fireworkEffect
                    val values: Array<FireworkEffect.Type> = FireworkEffect.Type.FireworkEffect.Type.entries.toTypedArray()
                    var index = (Arrays.asList(*values).indexOf(effect!!.type) + if (left) -1 else 1) % values.size
                    if (index < 0) index += values.size
                    val nextType = values[index]
                    settings.fireworkEffect = FireworkEffect.builder()
                            .with(nextType)
                            .flicker(effect.hasFlicker())
                            .trail(effect.hasTrail())
                            .withColor(effect.colors)
                            .withFade(effect.fadeColors).build()
                    Result.refresh()
                }, ClickType.RIGHT, ClickType.LEFT)) // Flicker
                .button(3, 1, Button.Builder().icon { p40: Player? ->
                    ItemBuilder
                            .fromSkull(if (settings.fireworkEffect!!.hasFlicker()) "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM4YWZiZmUzZmJkYmRkNTRlZTkxYWZlYTkxYTczY2ZjNjY2MzUyYzI3ZTcwNmYyYzM5MjE0MGY3MjAzMTI4YSJ9fX0=" else "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTBhZjMyMzhmNjNhYjIwYzU5YjE1OGY0MDQ3YmViNTVkYjExNmQxYTk0OThhZWE0YjlhZTU4MTk5MGZmOGQxNyJ9fX0=")
                            .name("&7Flicker (" + (if (settings.fireworkEffect!!.hasFlicker()) "&aon" else "&coff") + "&7)")
                            .build()
                }
                        .lmb { e: Button.Event? ->
                            // toggle
                            val effect = settings.fireworkEffect
                            settings.fireworkEffect = FireworkEffect.builder()
                                    .with(effect!!.type)
                                    .flicker(!effect.hasFlicker())
                                    .trail(effect.hasTrail())
                                    .withColor(effect.colors)
                                    .withFade(effect.fadeColors).build()
                            Result.refresh()
                        }
                ) // Trail
                .button(4, 1, Button.Builder().icon { p40: Player? ->
                    ItemBuilder
                            .fromSkull(if (settings.fireworkEffect!!.hasTrail()) "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQzMGM1YjkzNTgxNzlkMDk4Nzc0MGQ3NDc4YzBlZWI2YjljN2ZhMDdjZTQ4OGRkNjk4NTE4MWFmNjFmYjhhMiJ9fX0=" else "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg3ZDgzNWI1NDNlZDFiMDI0MTU3MDFjYTdiM2Y4YzhhMGExMTJhZjEzMThmOWNlYzVhNWU5MWU0ODE0YTI0OSJ9fX0=")
                            .name("&7Trail (" + (if (settings.fireworkEffect!!.hasTrail()) "&aon" else "&coff") + "&r&7)")
                            .build()
                }
                        .lmb { e: Button.Event? ->
                            // toggle
                            val effect = settings.fireworkEffect
                            settings.fireworkEffect = FireworkEffect.builder()
                                    .with(effect!!.type)
                                    .flicker(effect.hasFlicker())
                                    .trail(!effect.hasTrail())
                                    .withColor(effect.colors)
                                    .withFade(effect.fadeColors).build()
                            Result.refresh()
                        }
                ) // TODO add button to test out firework
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
                .parentButton(4, 2)

        // Add basic color button
        addColorButton(6, 1, Lang.EDITOR_FIREWORK_NAME_COLORS,
                { p0: Player? -> Lang.EDITOR_FIREWORK_TITLE_COLORS }, { obj: FireworkEffect? -> obj!!.colors }
        ) { fb: FireworkEffect.Builder, colors: List<Color?>? -> fb.withColor(colors!!).withFade(settings.fireworkEffect!!.fadeColors) }

        // Add fade color button
        addColorButton(7, 1, Lang.EDITOR_FIREWORK_NAME_FADE,
                { p0: Player? -> Lang.EDITOR_FIREWORK_TITLE_FADE }, { obj: FireworkEffect? -> obj!!.fadeColors }
        ) { fb: FireworkEffect.Builder, colors: List<Color?>? -> fb.withColor(settings.fireworkEffect!!.colors).withFade(colors!!) }
    }
}
