package com.crazicrafter1.lootcrates

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.crutils.ui.SimpleMenu.SBuilder
import com.crazicrafter1.crutils.ui.TextMenu.TBuilder
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Function

class ItemModifyMenu : SBuilder(5) {
    //private static final String BASE64_ARROW = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDRmN2JjMWZhODIxN2IxOGIzMjNhZjg0MTM3MmEzZjdjNjAyYTQzNWM4MjhmYWE0MDNkMTc2YzZiMzdiNjA1YiJ9fX0=";
    private var builder: ItemBuilder? = null
    fun build(it: ItemStack?, itemStackFunction: Function<ItemStack?, ItemStack>): ItemModifyMenu {
        builder = ItemBuilder.copy(it)
        return title { p: Player? -> Lang.EDITOR_EDIT_ITEM1 } //.background()
                .parentButton(0, 4) // color format description
                .button(8, 1, Button.Builder()
                        .icon { p: Player? -> ItemBuilder.copy(Material.PAPER).name("&c\u2191 &7Item raw/color").build() }
                ) // count description
                //.button(0, 1, new Button.Builder()
                //        .icon(p -> ItemBuilder.copy(Material.PAPER).name("&c\u2191 &7Item min/max").build())
                //)
                // color unformatted item
                .button(7, 0, Button.Builder()
                        .icon { p: Player? ->
                            val lore = ColorUtil.invertRendered(builder.getLoreString())
                            builder.copy().name(builder.getName(), ColorUtil.INVERT_RENDERED, "" + ChatColor.GRAY) // GRAY will only be applied to the first line
                                    // How to fix this
                                    .lore(if (lore != null) ChatColor.GRAY.toString() + java.lang.String.join("""
    
    ${ChatColor.GRAY}
    """.trimIndent(), *lore.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) else null, ColorUtil.AS_IS)
                                    .build()
                        }
                ) // color formatted item
                .button(8, 0, Button.Builder()
                        .icon { p: Player? -> builder.copy().placeholders(p).renderAll().build() }
                ) // material search
                .childButton(2, 3, { p: Player? -> ItemBuilder.copy(Material.COMPASS).name("&8Set material").lore("&7Search...").build() }, TBuilder()
                        .title { p: Player? -> "Material search" }
                        .leftRaw { p: Player? -> builder.getModernMaterial().lowercase(Locale.getDefault()) }
                        .onClose { player: Player? -> Result.parent() }
                        .onComplete(TriFunction<Player, String, TBuilder, BiConsumer<AbstractMenu, InventoryClickEvent>> { player: Player?, s: String?, tBuilder: TBuilder? ->
                            try {
                                //builder.material(builder.apply(builder));
                                // how to set material for legacy items
                                // set damage value
                                builder = ItemBuilder.copy(itemStackFunction.apply(builder.material(s).build()))
                                return@TriFunction Result.parent()
                            } catch (e: Exception) {
                                return@TriFunction Result.text("Does not exist")
                            }
                        })
                ) // edit Name
                .childButton(3, 1, { p: Player? -> ItemBuilder.copy(Material.NAME_TAG).name(Lang.EDITOR_NAME).lore(Lang.EDITOR_LMB_EDIT).build() }, TBuilder()
                        .title { p: Player? -> Lang.EDITOR_NAME }
                        .leftRaw { p: Player? -> builder.getNameOrLocaleName() }
                        .right({ p: Player? -> Lang.EDITOR_FORMATTING }, { p: Player? -> Editor.Companion.getColorDem() }, ColorUtil.AS_IS)
                        .onClose { player: Player? -> Result.parent() }
                        .onComplete { player: Player?, s: String, b: TBuilder? ->
                            if (s.isEmpty()) {
                                builder.removeName()
                            } else builder.name(s, ColorUtil.RENDER_MARKERS)
                            builder = ItemBuilder.copy(itemStackFunction.apply(builder.build()))
                            Result.parent()
                        }) // edit Lore                                                                                // terrible name
                .childButton(5, 1, { p: Player? -> ItemBuilder.from("WRITABLE_BOOK").hideFlags(ItemFlag.HIDE_POTION_EFFECTS).name(Lang.LORE).lore(Lang.EDITOR_LMB_EDIT).build() }, TBuilder()
                        .title { p: Player? -> Lang.LORE }
                        .leftRaw { p: Player? -> Util.def<String>(builder.getLoreString(), Editor.Companion.LOREM_IPSUM).replace("\n", "\\n") }
                        .right({ p: Player? -> Lang.EDITOR_FORMATTING }, { p: Player? -> Editor.Companion.getColorDem() }, ColorUtil.AS_IS)
                        .onClose { player: Player? -> Result.parent() }
                        .onComplete { player: Player?, s: String, b: TBuilder? ->
                            if (s.isEmpty()) {
                                builder.removeLore()
                            } else builder.lore(s.replace("\\n", "\n"), ColorUtil.RENDER_MARKERS)
                            builder = ItemBuilder.copy(itemStackFunction.apply(builder.build()))
                            Result.parent()
                        }) // Edit CustomModelData
                .childButton(4, 3, { p: Player? -> ItemBuilder.fromSkull(BASE64_CUSTOM_MODEL_DATA).name(Lang.EDITOR_ITEM_MODEL).lore(Lang.EDITOR_LMB_EDIT).build() }, TBuilder()
                        .title { p: Player? -> Lang.EDITOR_ITEM_MODEL }
                        .leftRaw { p: Player? ->
                            val meta = builder.getMeta()
                            if (meta != null && meta.hasCustomModelData()) return@leftRaw "" + meta.customModelData
                            Editor.Companion.LOREM_IPSUM
                        }
                        .right { p: Player? -> "&7" + Lang.EDITOR_ERROR_MODEL }
                        .onClose { player: Player? -> Result.parent() }
                        .onComplete { p: Player?, s: String, b: TBuilder? ->
                            if (s.isEmpty()) return@onComplete null
                            val i: Int
                            i = try {
                                s.toInt()
                            } catch (e00: Exception) {
                                return@onComplete Result.text(Lang.COMMAND_ERROR_INPUT)
                            }
                            builder.model(i)
                            builder = ItemBuilder.copy(itemStackFunction.apply(builder.build()))
                            Result.parent()
                        }, Version.AT_LEAST_v1_16.a()) // item swap
                .button(6, 3, Button.Builder() // if no custom name, don't even name
                        // just skip? or make an override to do nothing when null
                        //.icon(p -> builder.copy().name(builder.getName(), ColorUtil.RENDER_ALL, "" + ChatColor.GRAY).build())
                        .icon { p: Player? -> ItemBuilder.from("PLAYER_HEAD").name("&c&l[Item here]").build() }
                        .lmb { interact: Button.Event ->
                            if (interact.heldItem == null) {
                                return@lmb Result.message(Lang.EDITOR_ERROR_SWAP)
                            }
                            builder = ItemBuilder.copy(itemStackFunction.apply(interact.heldItem))
                            Result.grab().andThen(Result.refresh())
                        }) as ItemModifyMenu
    }

    companion object {
        private const val BASE64_CUSTOM_MODEL_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjU2NTJlYzMzYmI4YWJjNjMxNTA5M2Q1ZGZlMGYzNGQ0NzRjMjc3ZGE5YjBmMmE3MjZkNTA0ODY0ZTMxMDA5MyJ9fX0="
    }
}
