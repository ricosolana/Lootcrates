package com.crazicrafter1.lootcrates.crate

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.crutils.ui.ListMenu.LBuilder
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.lootcrates.*
import com.crazicrafter1.lootcrates.crate.loot.ILoot
import com.crazicrafter1.lootcrates.crate.loot.LootItem
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Function

class LootCollection(val id: String, var itemStack: ItemStack, loot: WeightedRandomContainer<ILoot>) {
    //public List<ILoot> loot;
    var loot: WeightedRandomContainer<ILoot>
    fun copy(): LootCollection {
        val strippedId: String = LCMain.NUMBER_AT_END.matcher(id).replaceAll("")
        var newId: String
        var i = 0
        while (PLUGIN!!.rewardSettings!!.lootSets!!.containsKey((strippedId + i).also { newId = it })) {
            i++
        }
        return LootCollection(newId, itemStack!!.clone(),
                WeightedRandomContainer<ILoot?>(loot.map.entries.stream().collect(CollectorUtils.toLinkedMap<Map.Entry<ILoot, Int?>, ILoot?, Int>(Function<Map.Entry<ILoot, Int?>, ILoot?> { (key): Map.Entry<ILoot, Int?> -> key.copy() }, Function<Map.Entry<ILoot, Int?>, Int> { (key, value) -> java.util.Map.Entry.value }))))
    }

    // <= rev 6
    constructor(id: String, item: ItemStack?, loot: List<ILoot?>?) : this(id, item, WeightedRandomContainer<ILoot?>(loot!!.stream().collect(CollectorUtils.toLinkedMap<ILoot?, ILoot?, Int>(Function<ILoot?, ILoot?> { k: ILoot? -> k }, Function<ILoot?, Int> { v: ILoot? -> 1 }))))

    // > rev 7 (item weights)
    init {
        this.loot = WeightedRandomContainer<ILoot>(
                loot.map.entries.stream().collect( //LinkedHashMap::new, (map, v) -> map.put(v, )
                        CollectorUtils.toLinkedMap<Map.Entry<ILoot?, Int>, ILoot, Int>(Function<Map.Entry<ILoot?, Int>, ILoot> { (key, value) -> java.util.Map.Entry.key }, Function<Map.Entry<ILoot?, Int>, Int> { (key, value) -> java.util.Map.Entry.value })))
    }

    fun serialize(section: ConfigurationSection) {
        section["item"] = itemStack
        //section.set("loot", loot.getMap());
        val result: MutableList<Map<String, Any>> = ArrayList()
        for ((key, value) in loot.map) {
            //section.set("loot");
            val map: MutableMap<String, Any> = HashMap()
            map["loot"] = key
            map["weight"] = value
            result.add(map)
        }
        section["loot"] = result
    }

    fun itemStack(@Nonnull p: Player?): ItemStack {
        return ItemBuilder.copy(itemStack)
                .placeholders(p)
                .renderAll()
                .build()
    }

    val randomLoot: ILoot
        get() = loot.random

    /**
     * Add the specified loot, and return it
     * @param iLoot loot
     * @return the loot instance
     */
    private fun addLoot(iLoot: ILoot, weight: Int): ILoot {
        loot.add(iLoot, weight)
        return iLoot
    }

    private fun getFormattedPercent(item: ILoot): String {
        return String.format("%.02f%%", 100f * (loot[item]!!.toFloat() / loot.totalWeight.toFloat()))
    }

    private fun getFormattedFraction(item: ILoot): String {
        return String.format("%d/%d", loot[item], loot.totalWeight)
    }

    val menuIcon: ItemStack
        get() =//return ItemBuilder.copy(itemStack)
//        .replace("lootset_id", id, '%')
//        .replace("lootset_size", "" + loot.getMap().size(), '%')
//        .lore(
//                  String.format(Lang.FORMAT_ID, id) + "\n"
//                + String.format(Lang.ED_LootSets_BTN_LORE, loot.getMap().size()) + "\n"
//                + Lang.ED_LMB_EDIT + "\n"
//                + Lang.ED_RMB_COPY + "\n"
//                + Lang.ED_RMB_SHIFT_DELETE
                //).build();
            ItemBuilder.copy(itemStack).lore(
                    """
                ${String.format(Lang.EDITOR_ID, id)}
                ${String.format(Lang.EDITOR_LOOT_LORE, loot.map.size)}
                ${Lang.EDITOR_LMB_EDIT}
                ${Lang.EDITOR_COPY}
                ${Lang.EDITOR_DELETE}
                """.trimIndent()
            ).build()
    val builder: AbstractMenu.Builder
        get() = LBuilder()
                .title { p: Player? -> id }
                .parentButton(4, 5) /*
                 * Add all Loot Items
                 */
                .addAll { self1: LBuilder?, p00: Player? ->
                    val result1 = ArrayList<Button>()
                    for ((a) in loot.map) {
                        val copy = a.getMenuIcon()
                        val menu = a.getMenuBuilder().title { p: Player? -> a.javaClass.getSimpleName() }
                        result1.add(Button.Builder()
                                .icon { p: Player? ->
                                    ItemBuilder.copy(copy)
                                            .lore("""${a.getMenuDesc()}
&7Weight: ${getFormattedFraction(a)} (${getFormattedPercent(a)}) - NUM
${Lang.EDITOR_LMB_EDIT}
${Lang.EDITOR_DELETE}
${Lang.EDITOR_COUNT_BINDS}
${Lang.EDITOR_COUNT_CHANGE}""").build()
                                }
                                .child(self1, menu)
                                .rmb { interact: Button.Event ->
                                    if (interact.shift && loot.map.size > 1) {
                                        // delete
                                        loot.remove(a)
                                        return@rmb Result.refresh()
                                    }
                                    null
                                }
                                .num { interact: Button.Event ->
                                    // weight modifiers
                                    val n = interact.numberKeySlot
                                    val change = if (n == 0) -5 else if (n == 1) -1 else if (n == 2) 1 else if (n == 3) 5 else 0
                                    if (change != 0) {
                                        // then change weight
                                        loot.add(a, MathUtil.clamp(loot[a]!! + change, 1, Int.MAX_VALUE))
                                    }
                                    Result.refresh()
                                }
                                .get())
                    }
                    result1
                }
                .childButton(3, 5, { p: Player? -> ItemBuilder.copy(itemStack).name(Lang.EDITOR_EDIT_ICON).lore(Lang.EDITOR_LMB_EDIT).build() }, ItemModifyMenu()
                        .build(itemStack, Function { itemStack: ItemStack? -> this.itemStack = itemStack }))
                .childButton(5, 5, { p: Player? -> ItemBuilder.copy(Material.GOLDEN_CARROT).name(Lang.NEW_LOOT).lore(Lang.EDITOR_LMB_ADD).build() }, LBuilder()
                        .title { p: Player? -> Lang.EDITOR_LOOT_NEW_TITLE }
                        .parentButton(4, 5)
                        .addAll { self1: LBuilder, p00: Player? ->
                            val result1 = ArrayList<Button>()
                            for ((key, value) in PLUGIN!!.lootClasses) {

                                //AbstractLoot aLootInstance = new a
                                result1.add(Button.Builder() // This causes a nullptr because it is instantly constructed?
                                        //.icon(() -> ItemBuilder.copyOf(Material.GOLD_INGOT).name(menuClazz.getSimpleName()).build())
                                        //.child(self1.parentMenuBuilder, lootSet.addLoot(
                                        //        (ILoot) ReflectionUtil.invokeConstructor(entry.getKey())).getMenuBuilder())
                                        .icon { p: Player? -> value }
                                        .lmb { interact: Button.Event? ->
                                            val menu = addLoot(
                                                    ReflectionUtil.invokeConstructor(key) as ILoot, 1).getMenuBuilder()
                                            menu!!.parent(self1.parentMenuBuilder)
                                                    .title { p: Player? -> menu!!.javaClass.getSimpleName() }
                                            Result.open(menu)
                                        }
                                        .get())
                            }
                            result1
                        }
                )
                .button(7, 5, Button.Builder().icon { p: Player? -> ItemBuilder.copy(Material.PAPER).name(Lang.LOOT_HELP_TITLE).lore(Lang.LOOT_HELP_SUB).build() })
                .capture(Button.Builder().lmb { e: Button.Event ->
                    if (e.heldItem != null) {
                        addLoot(LootItem(e.heldItem), 1)
                        return@lmb Result.refresh()
                    }
                    Result.ok()
                })

    companion object {
        private val PLUGIN: LCMain? = LCMain.Companion.get()
    }
}
