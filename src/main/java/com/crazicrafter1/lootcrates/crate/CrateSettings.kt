package com.crazicrafter1.lootcrates.crate

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.crutils.ui.ListMenu.LBuilder
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.crutils.ui.SimpleMenu.SBuilder
import com.crazicrafter1.crutils.ui.TextMenu.TBuilder
import com.crazicrafter1.lootcrates.*
import de.tr7zw.nbtapi.NBT
import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

class CrateSettings(val id: String, var title: String?, var columns: Int, var picks: Int, var sound: Sound, loot: Map<String?, Int?>?, item: ItemStack?, revealType: RevealType) {
    enum class RevealType {
        GOOD_OL_DESTY,
        WASD,
        CSGO,
        POPCORN
    }

    var revealType: RevealType

    // TODO deleting the lootset in editor will cause an error when crate is opened
    // because the string referencing to key in map will be removed
    // should use WeakReference
    //private WeightedRandomContainer<String> loot;
    var loot: WeightedRandomContainer<String?>
    var item: ItemStack?
    fun copy(): CrateSettings {
        val strippedId: String = LCMain.Companion.NUMBER_AT_END.matcher(id).replaceAll("")
        var newId: String
        var i = 0
        while (LCMain.Companion.get()!!.rewardSettings!!.crates!!.containsKey((strippedId + i).also { newId = it })) {
            i++
        }
        return CrateSettings(newId, title, columns, picks, sound, HashMap(loot.map), Lootcrates.tagItemAsCrate(item!!.clone(), newId), revealType)
    }

    //todo hmmm kinda ugly
    //@Deprecated
    //public CrateSettings(String id) {
    //    this.id = id;
    //    this.title = "select loot";
    //    this.columns = 3;
    //    this.picks = 4;
    //    this.sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    //    this.loot = new WeightedRandomContainer<>();
    //    this.item = ItemBuilder.mut(LootcratesAPI.getCrateAsItem(new ItemStack(Material.ENDER_CHEST), id)).name("my new crate").build();
    //}
    //todo remove post-migrate
    init {
        this.loot = WeightedRandomContainer(loot)
        this.item = item
        this.revealType = revealType
    }

    //public CrateSettings(String id, Map<String, Object> args) {
    //    this.id = id;
    //    this.title = ColorUtil.renderMarkers((String) args.get("title"));
    //    this.columns = (int) args.get("columns");
    //    this.picks = (int) args.get("picks");
    //    this.sound = Sound.valueOf((String) args.get("sound"));
    //    this.loot = new WeightedRandomContainer<>((Map<LootSetSettings, Integer>) args.get("weights"));
    //    this.item = (ItemStack) args.get("item");
    //}
    fun serialize(section: ConfigurationSection) {
        section["item"] = item
        section["title"] = ColorUtil.invertRendered(title)
        section["columns"] = columns
        section["picks"] = picks
        section["sound"] = sound.name
        section["weights"] = loot.map.entries.stream().collect(Collectors.toMap<Map.Entry<String?, Int>, String, Int>(Function<Map.Entry<String?, Int>, String> { (key, value) -> java.util.Map.Entry.key }, Function<Map.Entry<String?, Int>, Int> { (key, value) -> java.util.Map.Entry.value }))
        section["revealType"] = revealType.name
    }

    private fun getFormattedPercent(lootGroup: LootCollection?): String {
        return String.format("%.02f%%", 100f * (loot[lootGroup!!.id]!!.toFloat() / loot.totalWeight.toFloat()))
    }

    private fun getFormattedFraction(lootGroup: LootCollection?): String {
        return String.format("%d/%d", loot[lootGroup!!.id], loot.totalWeight)
    }

    val randomLootSet: LootCollection?
        get() = LCMain.Companion.get()!!.rewardSettings!!.lootSets!!.get(loot.random)
    val menuIcon: ItemStack
        /**
         * Remove the lootset by id if present
         * If the last lootset was removed from this crate then a blank
         * @param
         */
        get() = ItemBuilder.copy(item).renderAll().lore(
                """
            ${String.format(Lang.EDITOR_ID, id)}
            ${Lang.EDITOR_LMB_EDIT}
            ${Lang.EDITOR_COPY}
            ${Lang.EDITOR_DELETE}
            """.trimIndent()).build()

    /**
     * Return the macro formatted item, or unformatted if player is null
     * @param p player
     * @return the formatted item
     */
    fun itemStack(p: Player?): ItemStack {
        val itemStack = ItemBuilder.copy(item)
                .replace("crate_picks", "" + picks, '%')
                .placeholders(p)
                .renderAll()
                .build()
        if (LCMain.Companion.get()!!.checkCerts) {
            val nbt = NBT.itemStackToNBT(itemStack)
            val tag = nbt.getCompound("tag")
            val ticket = UUID.randomUUID()
            tag.setUUID("CrateCert", ticket)
            LCMain.Companion.crateCerts.add(ticket)
            return NBT.itemStackFromNBT(nbt)
        }
        return itemStack
    }

    fun getTitle(p: Player?): String {
        return ColorUtil.renderAll(Util
                .placeholders(p, title
                        .replace("%crate_picks%", "" + picks)
                ))
    }

    override fun toString(): String {
        return """
            id: $id
            itemStack: $item
            title: $title
            size: $title
            picks: $picks
            sound: $sound
            weights: $loot
            revealType${revealType.name}
            
            """.trimIndent()
    }

    val builder: AbstractMenu.Builder
        get() = SBuilder(5)
                .title { p: Player? -> id }
                .background() //.onOpen(p -> p.setGameMode(GameMode.CREATIVE))
                //.onClose(p -> {
                //    p.setGameMode();
                //    return Result.PARENT();
                //})
                .parentButton(4, 4) // *   *   *
                // Edit Crate ItemStack
                // *   *   *
                .childButton(1, 1, { p: Player? -> ItemBuilder.copy(item).name(Lang.EDITOR_EDIT_ITEM).lore(Lang.EDITOR_LMB_EDIT).build() }, ItemModifyMenu()
                        .build(item) { itemStack: ItemStack? ->
                            ItemBuilder.mut(item)
                                    .apply(itemStack!!,
                                            ItemBuilder.FLAG_NAME or ItemBuilder.FLAG_LORE or ItemBuilder.FLAG_SKULL or ItemBuilder.FLAG_MATERIAL)
                                    .build()
                        }
                ) // Edit Inventory Title
                .childButton(3, 1, { p: Player? -> ItemBuilder.copy(Material.PAPER).name(String.format(Lang.EDITOR_EDIT_TITLE, title)).lore(Lang.EDITOR_LMB_EDIT).build() }, TBuilder()
                        .title { p: Player? -> Lang.EDITOR_TITLE1 }
                        .leftRaw { p: Player? -> title }
                        .onClose { player: Player? -> Result.parent() }
                        .right({ p: Player? -> Lang.EDITOR_FORMATTING }, { p: Player? -> Editor.Companion.getColorDem() }, ColorUtil.AS_IS)
                        .onComplete { p: Player?, s: String, b: TBuilder? ->
                            if (!s.isEmpty()) {
                                title = ColorUtil.RENDER_MARKERS.a(s)
                                return@onComplete Result.parent()
                            }
                            Result.text(Lang.COMMAND_ERROR_INPUT)
                        }
                ) // *   *   *
                // Edit LootSets
                // *   *   *
                .childButton(5, 1, { p: Player? -> ItemBuilder.from("EXPERIENCE_BOTTLE").name(Lang.EDITOR_LOOT1).lore(Lang.EDITOR_LMB_EDIT).build() }, LBuilder()
                        .title { p: Player? -> Lang.EDITOR_LOOT1 }
                        .parentButton(4, 5)
                        .onClose { player: Player? -> Result.parent() }
                        .addAll { builder: LBuilder?, p: Player? ->
                            val result1 = ArrayList<Button>()
                            for (lootSet in LCMain.Companion.get()!!.rewardSettings!!.lootSets!!.values) {
                                val weight = loot[lootSet!!.id]
                                val btn = Button.Builder()
                                val b = ItemBuilder.copy(lootSet!!.itemStack!!.type).name("&8" + lootSet!!.id)
                                if (weight != null) {
                                    b.lore("""&7Weight: ${getFormattedFraction(lootSet)} (${getFormattedPercent(lootSet)}) - NUM
${Lang.EDITOR_LMB_TOGGLE}
${Lang.EDITOR_COUNT_BINDS}
${Lang.EDITOR_COUNT_CHANGE}""").glow(true)
                                    btn.lmb { interact: Button.Event? ->
                                        if (loot.map.size > 1) {
                                            // toggle inclusion
                                            loot.remove(lootSet!!.id)
                                            return@lmb Result.refresh()
                                        }
                                        null
                                    }.num { interact: Button.Event ->
                                        // weight modifiers
                                        val n = interact.numberKeySlot
                                        val change = if (n == 0) -5 else if (n == 1) -1 else if (n == 2) 1 else if (n == 3) 5 else 0
                                        if (change != 0) {
                                            // then change weight
                                            loot.add(lootSet!!.id, MathUtil.clamp(weight + change, 1, Int.MAX_VALUE))
                                        }
                                        Result.refresh()
                                    }
                                } else {
                                    b.lore(Lang.EDITOR_LMB_TOGGLE)
                                    btn.lmb { interact: Button.Event? ->
                                        loot.add(lootSet!!.id, 1)
                                        Result.refresh()
                                    }
                                }
                                result1.add(btn.icon { p1: Player? -> b.build() }.get())
                            }
                            result1
                        }
                ) // *   *   *
                // Edit Columns
                // *   *   *
                .button(7, 1, Button.Builder()
                        .icon { p: Player? ->
                            ItemBuilder.copy(Material.LADDER).name(String.format(Lang.EDITOR_CRATE_COLUMNS, columns)).lore("""
    ${Lang.EDITOR_LMB_DECREMENT}
    ${Lang.EDITOR_INCREMENT}
    """.trimIndent()).amount(columns).build()
                        }
                        .lmb { interact: Button.Event? ->
                            // decrease
                            columns = MathUtil.clamp(columns - 1, 1, 6)
                            Result.refresh()
                        }
                        .rmb { interact: Button.Event? ->
                            // decrease
                            columns = MathUtil.clamp(columns + 1, 1, 6)
                            Result.refresh()
                        }) // *   *   *
                // Edit Picks
                // *   *   *
                .button(2, 3, Button.Builder()
                        .icon { p: Player? ->
                            ItemBuilder.copy(Material.MELON_SEEDS).name(String.format(Lang.EDITOR_CRATE_PICKS, picks)).lore("""
    ${Lang.EDITOR_LMB_DECREMENT}
    ${Lang.EDITOR_INCREMENT}
    """.trimIndent()).amount(picks).build()
                        }
                        .lmb { interact: Button.Event? ->
                            // decrease
                            picks = MathUtil.clamp(picks - 1, 1, columns * 9)
                            Result.refresh()
                        }
                        .rmb { interact: Button.Event? ->
                            // decrease
                            picks = MathUtil.clamp(picks + 1, 1, columns * 9)
                            Result.refresh()
                        }) // *   *   *
                // Edit Pick Sound
                // *   *   *
                .childButton(6, 3, { p: Player? -> ItemBuilder.copy(Material.JUKEBOX).name(String.format(Lang.EDITOR_CRATE_SOUND, sound)).lore(Lang.EDITOR_LMB_EDIT).build() },
                        TBuilder()
                                .title { p: Player? -> Lang.EDITOR_CRATE_SOUND_TITLE }
                                .leftRaw { p: Player? -> Editor.Companion.LOREM_IPSUM }
                                .right { p: Player? -> Lang.EDITOR_CRATE_SOUND_INPUT }
                                .onClose { player: Player? -> Result.parent() }
                                .onComplete { p: Player, s: String, b: TBuilder? ->
                                    try {
                                        sound = Sound.valueOf(s.uppercase(Locale.getDefault()))
                                        p.playSound(p.location, sound, 1f, 1f)
                                        return@onComplete Result.parent()
                                    } catch (e: Exception) {
                                        return@onComplete Result.text(Lang.COMMAND_ERROR_INPUT)
                                    }
                                }
                )
    val preview: AbstractMenu.Builder
        get() = LBuilder()
                .title { p: Player? -> "Preview" }
                .addAll { self: LBuilder?, p: Player? ->
                    val buttons: MutableList<Button> = ArrayList()
                    for ((key, weight) in loot.map) {
                        val lootSet: LootCollection = LCMain.Companion.get()!!.rewardSettings!!.lootSets!!.get(key)
                        val chance = weight.toDouble() / loot.totalWeight.toDouble() * 100.0
                        buttons.add(Button.Builder()
                                .icon { p00: Player? -> ItemBuilder.copy(lootSet.itemStack).lore(String.format("&8%.02f%%", chance)).build() }.get())
                    }
                    buttons
                }
}
