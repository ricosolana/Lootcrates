package com.crazicrafter1.lootcrates

import com.crazicrafter1.crutils.*
import com.crazicrafter1.lootcrates.crate.CrateSettings
import com.crazicrafter1.lootcrates.crate.CrateSettings.RevealType
import com.crazicrafter1.lootcrates.crate.LootCollection
import com.crazicrafter1.lootcrates.crate.loot.ILoot
import com.crazicrafter1.lootcrates.crate.loot.LootItem
import org.apache.commons.lang3.Validate
import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class RewardSettings {
    var speed = 0
    var autoCloseTime = 0 // -1 to never close, 0 to close immediately after final loot, else x(s) after last loot
    lateinit var unSelectedItem: ItemStack
    lateinit var selectedItem: ItemStack
    var fireworkEffect: FireworkEffect? = null
    lateinit var crates: MutableMap<String, CrateSettings>
    lateinit var lootSets: MutableMap<String, LootCollection>

    constructor(speed: Int, autoCloseTime: Int, unSelectedItem: ItemStack, selectedItem: ItemStack, fireworkEffect: FireworkEffect?, crates: MutableMap<String, CrateSettings>, lootSets: MutableMap<String, LootCollection>) {
        this.speed = speed
        this.autoCloseTime = autoCloseTime
        this.unSelectedItem = unSelectedItem
        this.selectedItem = selectedItem
        this.fireworkEffect = fireworkEffect
        this.crates = crates
        this.lootSets = lootSets
    }

    /**
     * fallback
     */
    constructor() {
        speed = 4
        autoCloseTime = -1
        unSelectedItem = ItemBuilder.copy(Material.CHEST).name("&f&l???").lore("&7&oChoose 4 mystery chests, and\n&7&oyour loot will be revealed!").build()
        selectedItem = ItemBuilder.from("WHITE_STAINED_GLASS_PANE").name("&7&l???").lore("&7You have selected this mystery chest").build()
        lootSets = LinkedHashMap()
        val lootSet = LootCollection(
                "common",
                ItemBuilder.from("WHITE_STAINED_GLASS_PANE").name("&f&lCommon Reward").build(),
                ArrayList<ILoot?>(listOf(LootItem())))
        lootSets[lootSet.id] = lootSet
        crates = HashMap()
        crates["peasant"] = Lootcrates.createCrate("peasant")
        fireworkEffect = FireworkEffect.builder().withColor(Color.RED, Color.BLUE, Color.WHITE).with(FireworkEffect.Type.BURST).build()
    }

    constructor(section: ConfigurationSection) {
        try {
            val rev: Int = LCMain.get().rev
            speed = section.getInt("speed", 4)
            autoCloseTime = section.getInt("auto-close-time", -1)
            unSelectedItem = section.getItemStack("unSelectedItem")!! //, String.format(Lang.CONFIG_ERROR4, "unSelectedItem"))
            selectedItem = section.getItemStack("selectedItem")!! //, String.format(Lang.CONFIG_ERROR4, "selectedItem"))
            lootSets = LinkedHashMap()
            for ((id, value) in section.getConfigurationSection("lootSets")!!.getValues(false)) {
                val itr = (value as ConfigurationSection).getValues(false)
                val itemStack = Objects.requireNonNull(itr["item"] as ItemStack?, String.format(Lang.CONFIG_ERROR4, "'lootSets.<$id>.item'"))
                if (rev <= 6) {
                    val loot = itr["loot"] as List<*>
                    val msg: () -> String = {
                        String.format(Lang.CONFIG_ERROR4, "'lootSets.<$id>.loot[]'")
                    }
                    check(null !in loot, msg)
                    lootSets[id] = LootCollection(id, itemStack, loot)
                } else {
                    val list = itr["loot"] as List<Map<String, Any>>?
                    val result: MutableMap<ILoot?, Int?> = LinkedHashMap()
                    for (sub in list!!) {
                        result[sub["loot"] as ILoot?] = sub["weight"] as Int
                    }

                    // write the loot as a list containing sub-maps of item, weight
                    Validate.isTrue(!result.containsKey(null), String.format(Lang.CONFIG_ERROR4, "'lootSets.<$id>.loot.data'"))
                    Validate.isTrue(!result.containsValue(0), String.format(Lang.CONFIG_ERROR7, "'lootSets.<$id>.loot.weight'"))
                    Validate.isTrue(!result.containsValue(null), String.format(Lang.CONFIG_ERROR4, "'lootSets.<$id>.loot.weight'"))
                    lootSets[id] = LootCollection(id, itemStack, WeightedRandomContainer(result))
                }
            }
            crates = LinkedHashMap()
            for ((id, value) in section.getConfigurationSection("crates")!!.getValues(false)) {
                val itr = (value as ConfigurationSection).getValues(false)
                val map = (itr["weights"] as ConfigurationSection?)!!.getValues(false)
                val weights = map.entries.stream()
                        .collect(Collectors.toMap<Map.Entry<String, Any>, String?, Int?>(Function { (key): Map.Entry<String, Any> -> Objects.requireNonNull(lootSets.get(key), key + "missing reference to LootSet '" + key + "' in 'crates.<" + id + ">.weights.<" + key + ">'").id },
                                Function { (_, value1): Map.Entry<String, Any> -> value1 as Int }))
                crates[id] = CrateSettings(id,
                        Objects.requireNonNull(itr["title"] as String?, String.format(Lang.CONFIG_ERROR4, "'crates.<$id>.title'")),
                        itr["columns"] as Int,
                        itr["picks"] as Int,
                        Sound.valueOf((itr["sound"] as String?)!!),
                        weights,
                        Lootcrates.tagItemAsCrate(Objects.requireNonNull(itr["item"] as ItemStack?, String.format(Lang.CONFIG_ERROR4, "'crates.<$id>.item'")), id),
                        if (rev >= 8) RevealType.valueOf((itr["revealType"] as String?)!!) else RevealType.GOOD_OL_DESTY
                )
            }
            fireworkEffect = section["fireworkEffect"] as FireworkEffect?
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun serialize(section: ConfigurationSection) {
        try {
            section["speed"] = speed
            section["auto-close-time"] = autoCloseTime
            section["unSelectedItem"] = unSelectedItem
            section["selectedItem"] = selectedItem
            lootSets!!.forEach { (k: String?, v: LootCollection?) -> v!!.serialize(section.createSection("lootSets.$k")) }
            crates!!.forEach { (k: String?, v: CrateSettings?) -> v!!.serialize(section.createSection("crates.$k")) }
            section["fireworkEffect"] = fireworkEffect
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e.message)
        }
    }

    fun unSelectedItemStack(@Nonnull p: Player?, @Nonnull crate: CrateSettings?): ItemStack {
        return ItemBuilder.copy(unSelectedItem)
                .replace("crate_picks", "" + crate!!.picks, '%')
                .placeholders(p)
                .renderAll()
                .build()
    }

    fun selectedItemStack(@Nonnull p: Player?, @Nonnull crate: CrateSettings?): ItemStack {
        return ItemBuilder.copy(selectedItem)
                .replace("crate_picks", "" + crate!!.picks, '%')
                .placeholders(p)
                .renderAll()
                .build()
    }
}
