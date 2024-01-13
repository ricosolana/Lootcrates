package com.crazicrafter1.lootcrates

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.crutils.ui.ListMenu.LBuilder
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.crutils.ui.SimpleMenu.SBuilder
import com.crazicrafter1.crutils.ui.TextMenu.TBuilder
import com.crazicrafter1.lootcrates.crate.LootCollection
import com.crazicrafter1.lootcrates.crate.loot.ILoot
import com.crazicrafter1.lootcrates.crate.loot.LootItem
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.regex.Pattern

class Editor {
    fun open(p000: Player) {
        if (p000.gameMode != GameMode.CREATIVE) {
            PLUGIN!!.notifier!!.warn(p000, Lang.MESSAGE_EDITOR_OPEN)
        }
        val settings = PLUGIN!!.rewardSettings
        SBuilder(3)
                .title { p: Player? -> Lang.EDITOR_TITLE }
                .background() /* *************** *\
                *                   *
                * Global Crate List *
                *                   *
                \* *************** */
                .childButton(2, 1, { p: Player? -> ItemBuilder.copy(Material.CHEST).name(Lang.EDITOR_CRATE).build() }, LBuilder()
                        .title { p: Player? -> Lang.EDITOR_CRATE_TITLE }
                        .parentButton(4, 5) // *       *      *
                        // Add Crate button
                        // *       *      *
                        .childButton(5, 5, { p: Player? -> ItemBuilder.copy(Material.END_CRYSTAL).name(Lang.EDITOR_CRATE_NEW).build() }, TBuilder()
                                .title { p: Player? -> Lang.EDITOR_CRATE_NEW_TITLE }
                                .leftRaw { p: Player? -> LOREM_IPSUM }
                                .onClose { player: Player? -> Result.parent() }
                                .onComplete { player: Player?, s: String, b: TBuilder? ->
                                    var s = s
                                    s = NON_ASCII_PATTERN.matcher(s.replace(" ", "_")).replaceAll("").lowercase(Locale.getDefault())
                                    if (s.isEmpty()) return@onComplete Result.text(Lang.EDITOR_ERROR9)

                                    // if crate already exists
                                    val crate = Lootcrates.getCrate(s)
                                    if (crate != null) return@onComplete Result.text(Lang.EDITOR_CRATE_ERROR1)
                                    Lootcrates.registerCrate(Lootcrates.createCrate(s))
                                    Result.parent()
                                }
                        )
                        .addAll { self: LBuilder?, p00: Player? ->
                            val result = ArrayList<Button>()
                            for ((_, crate) in settings!!.crates!!) {
                                result.add(Button.Builder() // https://regexr.com/6fdsi
                                        .icon { p: Player? -> crate.menuIcon }
                                        .child(self, crate.builder) // Shift-RMB - delete crate
                                        .bind(ClickType.SHIFT_RIGHT) { event: Button.Event? ->
                                            settings.crates!!.remove(crate!!.id)
                                            Result.refresh()
                                        } // RMB - clone crate
                                        .bind(ClickType.RIGHT) { event: Button.Event? ->
                                            val copy = crate!!.copy()
                                            Lootcrates.registerCrate(copy)
                                            Result.refresh()
                                        }.get()
                                )
                            }
                            result
                        } /*
                 * View LootSets
                 */
                ).childButton(4, 1, { p: Player? -> ItemBuilder.from("EXPERIENCE_BOTTLE").name(Lang.EDITOR_LOOT).build() }, LBuilder()
                        .title { p: Player? -> Lang.EDITOR_LOOT_TITLE }
                        .parentButton(4, 5) /*
                         * Each Collection
                         */
                        .addAll { self: LBuilder?, p1: Player? ->
                            val result = ArrayList<Button>()
                            for (lootSet in settings!!.lootSets!!.values) {
                                /*
                                 * Add Collections
                                 */
                                result.add(Button.Builder()
                                        .icon { p: Player? -> lootSet.menuIcon }
                                        .child(self, lootSet.builder) // LMB - Edit Loot Collection
                                        // Shift-RMB - delete lootset
                                        .bind(ClickType.SHIFT_RIGHT) { event: Button.Event? ->
                                            if (!Lootcrates.removeLootSet(lootSet!!.id)) return@bind Result.message("Failed to remove LootSet")
                                            Result.refresh()
                                        } // RMB - clone lootset
                                        .bind(ClickType.RIGHT) { event: Button.Event? ->
                                            val copy = lootSet!!.copy()
                                            settings.lootSets!![copy!!.id] = copy
                                            Result.refresh()
                                        }
                                        .get()
                                )
                            }
                            result
                        } /*
                         * Add custom Collection
                         */
                        .childButton(5, 5, { p: Player? -> ItemBuilder.copy(Material.NETHER_STAR).name(Lang.EDITOR_LOOT_NEW).build() }, TBuilder()
                                .title { p: Player? -> Lang.EDITOR_LOOT_ADD_TITLE }
                                .leftRaw { p: Player? -> LOREM_IPSUM } // id
                                .onClose { player: Player? -> Result.parent() }
                                .onComplete { player: Player?, s: String, b: TBuilder? ->
                                    var s = s
                                    s = NON_ASCII_PATTERN.matcher(s.replace(" ", "_")).replaceAll("").lowercase(Locale.getDefault())
                                    if (s.isEmpty()) return@onComplete Result.text(Lang.EDITOR_ERROR9)
                                    if (settings!!.crates!!.containsKey(s)) return@onComplete Result.text(Lang.EDITOR_CRATE_ERROR1)
                                    settings.lootSets!![s] = LootCollection(s, ItemStack(Material.GLOWSTONE_DUST),
                                            ArrayList<ILoot?>(listOf(LootItem())))
                                    Result.parent()
                                }
                        )
                ) /*
                 * Fireworks Editor
                 */
                .childButton(6, 1, { p: Player? -> ItemBuilder.from("FIREWORK_ROCKET").name(Lang.EDITOR_FIREWORK).build() }, FireworkModifyMenu())
                .open(p000)
    }

    companion object {
        const val LOREM_IPSUM = "Lorem ipsum"
        private val COLORS = """${ColorUtil.renderMarkers("&a" + LOREM_IPSUM)}
${ChatColor.WHITE}   : ${ChatColor.GRAY}&a$LOREM_IPSUM
${ColorUtil.renderMarkers("&#456789" + LOREM_IPSUM)}
${ChatColor.WHITE}   : ${ChatColor.GRAY}&#456789$LOREM_IPSUM
${ColorUtil.renderAll("<#aa7744>" + LOREM_IPSUM + "</#abcdef>")}
${ChatColor.WHITE}   : ${ChatColor.GRAY}<#aa7744>$LOREM_IPSUM</#abcdef>
${ColorUtil.renderAll("<#555555>&8-------&7------&f---</#bbbbbb><#bbbbbb>&f---&7------&8-------</#555555>")}
"""
        val colorDem: String
            get() = (COLORS
                    + ColorUtil.renderAll("""${String.format(Lang.EDITOR_SUPPORTS, "PlaceholderAPI")}
${Lang.EDITOR_ITEM_MACROS}
${ChatColor.WHITE}   : ${ChatColor.GRAY}%crate_picks%
${Lang.EDITOR_LORES}
${ChatColor.WHITE}   : ${ChatColor.GRAY}\n"""))
        const val BASE64_DEC = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM1YThhYThhNGMwMzYwMGEyYjVhNGViNmJlYjUxZDU5MDI2MGIwOTVlZTFjZGFhOTc2YjA5YmRmZTU2NjFjNiJ9fX0="
        const val BASE64_INC = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFiOTVhODc1MWFlYWEzYzY3MWE4ZTkwYjgzZGU3NmEwMjA0ZjFiZTY1NzUyYWMzMWJlMmY5OGZlYjY0YmY3ZiJ9fX0="
        val NON_ASCII_PATTERN = Pattern.compile("[^a-zA-Z0-9_.]+")
        private val PLUGIN: LCMain? = LCMain.Companion.get()
    }
}
