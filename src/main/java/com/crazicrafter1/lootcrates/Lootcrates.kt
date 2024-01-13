package com.crazicrafter1.lootcrates

import com.crazicrafter1.crutils.*
import com.crazicrafter1.lootcrates.crate.CrateInstance
import com.crazicrafter1.lootcrates.crate.CrateSettings
import com.crazicrafter1.lootcrates.crate.CrateSettings.RevealType
import com.crazicrafter1.lootcrates.crate.LootCollection
import de.tr7zw.nbtapi.NBT
import org.apache.commons.lang3.Validate
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

object Lootcrates {
    //public static Map<Class<? extends ILoot>, ItemStack> lootClasses = new HashMap<>();
    // Call only during init
    //static void registerLoot(@Nonnull Class<? extends ILoot> lootClass) {
    //    lootClasses.put(lootClass, (ItemStack) ReflectionUtil.getFieldInstance(ReflectionUtil.getField(lootClass, "EDITOR_ICON"), null));
    //    ConfigurationSerialization.registerClass(lootClass, lootClass.getSimpleName());
    //    Main.get().notifier.info("Registering " + lootClass.getSimpleName());
    //}
    //public LootSetSettings getRandomLoot(CrateSettings crateSettings) {
    //    return crateSettings.
    //}
    private const val CRATE_KEY = "Crate"
    private const val CRATE_CERT_KEY = "CrateCert"
    private const val TAG_KEY = "tag"
    private val PLUGIN: LCMain? = LCMain.Companion.get()

    // TODO I do not like how this loot set reduction and append is handled
    //  too unsafe and tacky
    @Deprecated("")
    fun removeLootSet(id: String?, failIfReferenced: Boolean): Boolean {
        if (PLUGIN!!.rewardSettings!!.lootSets!!.size > 1) {
            if (failIfReferenced) {
                val it = crates
                while (it.hasNext()) {
                    val crate = it.next()
                    // check for crates that use this loot set
                    // if used by any, then fail
                    if (crate!!.loot[id] != null) return false
                }
            }
            val lootCollection = PLUGIN.rewardSettings!!.lootSets!!.remove(id) ?: return false
            val it = crates
            while (it.hasNext()) {
                val crate = it.next()
                crate!!.loot.remove(id)
                if (crate.loot.map.isEmpty()) {
                    val next = PLUGIN.rewardSettings!!.lootSets!!.values.iterator().next()
                    crate.loot.add(next!!.id, 1)
                }
            }
            return true
        }
        return false
    }

    /**
     * Removes a lootSet by id (deletes it)
     * References will be removed from crates and replaced if loot is empty
     * @param id lootSet id
     * @return whether successful
     */
    fun removeLootSet(id: String?): Boolean {
        if (PLUGIN!!.rewardSettings!!.lootSets!!.size > 1) {
            val lootCollection = PLUGIN.rewardSettings!!.lootSets!!.remove(id) ?: return false
            val it = crates
            while (it.hasNext()) {
                val crate = it.next()
                crate!!.loot.remove(id)

                // if crate has no loot, add the next loot
                if (crate.loot.map.isEmpty()) {
                    val next = loot.next()
                    crate.loot.add(next!!.id, 1)
                }
            }
            return true
        }
        return false
    }

    fun createCrate(id: String): CrateSettings {
        val map: MutableMap<String?, Int?> = HashMap()
        map[loot.next()!!.id] = 1
        return CrateSettings(id, "select loot", 3, 4, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                map,
                ItemBuilder.mut(tagItemAsCrate(ItemStack(Material.ENDER_CHEST), id)).name("my new crate").build(),
                RevealType.GOOD_OL_DESTY
        )
    }

    fun registerCrate(crateSettings: CrateSettings?) {
        PLUGIN!!.rewardSettings!!.crates!![crateSettings!!.id] = crateSettings
    }

    fun getCrate(@Nonnull id: String?): CrateSettings? {
        return PLUGIN!!.rewardSettings!!.crates!![id]
    }

    fun getCrate(itemStack: ItemStack?): CrateSettings? {
        if (itemStack == null || itemStack.type == Material.AIR) return null

        // item.nbt["tag"]["Crate"] (pseudocode)
        val tag = NBT.itemStackToNBT(itemStack).getCompound(TAG_KEY) ?: return null
        return getCrate(tag.getString(CRATE_KEY))

        //INBTTagCompound nbt = NMSAPI.getNBT(itemStack);
        //if (nbt == null) return null;
        //return getCrate(nbt.getString("Crate"));
    }

    @Nonnull
    fun tagItemAsCrate(@Nonnull itemStack: ItemStack?, @Nonnull id: String): ItemStack {
        Validate.notNull(id)
        val nbt = NBT.itemStackToNBT(itemStack)
        val tag = nbt.getOrCreateCompound(TAG_KEY)
        tag.setString(CRATE_KEY, id)
        return NBT.itemStackFromNBT(nbt)

        //INBTTagCompound nbt = NMSAPI.getOrCreateNBT(itemStack);
        //nbt.setString("Crate", id);
        //return nbt.setNBT(itemStack);
    }

    /**
     * Try to claim the UUID ticket attached to a crate (to prevent duplication)
     * This can modify the legal ticket pool
     * @param itemStack crate item
     * @return The attached UUID or null if illegal
     */
    fun claimTicket(itemStack: ItemStack?): UUID? {
        val tag = NBT.itemStackToNBT(itemStack).getCompound(TAG_KEY) ?: return null
        // hmm
        val ticket = tag.getUUID(CRATE_CERT_KEY)
        return if (LCMain.Companion.crateCerts.remove(ticket)) ticket else null
    }

    /**
     * If the attacked ticket to the crate ItemStack can be claimed
     * @param itemStack crate item
     * @return The attached UUID or null if illegal
     */
    fun canClaimTicket(itemStack: ItemStack?): UUID? {
        val tag = NBT.itemStackToNBT(itemStack).getCompound(TAG_KEY) ?: return null
        // hmm
        val ticket = tag.getUUID(CRATE_CERT_KEY)
        return if (LCMain.Companion.crateCerts.contains(ticket)) ticket else null
    }

    val crates: Iterator<CrateSettings?>
        get() = PLUGIN!!.rewardSettings!!.crates!!.values.iterator()
    val loot: Iterator<LootCollection?>
        get() = PLUGIN!!.rewardSettings!!.lootSets!!.values.iterator()

    // TODO add force-or prevention check
    fun showCrate(@Nonnull p: Player?, @Nonnull crate: CrateSettings?): Boolean {
        if (CrateInstance.Companion.CRATES.containsKey(p!!.uniqueId)) return false
        CrateInstance(p, crate, null).open()
        return true
    }

    fun showPreview(@Nonnull p: Player?, @Nonnull crate: CrateSettings?): Boolean {
        if (CrateInstance.Companion.CRATES.containsKey(p!!.uniqueId)) return false
        crate.getPreview().open(p)
        return true
    }

    /**
     * Closes a player-opened crate
     * @param p [Player] instance
     * @return whether the close was successful
     */
    fun stopCrate(@Nonnull p: Player): Boolean {
        val activeCrate: CrateInstance = CrateInstance.Companion.CRATES.remove(p.uniqueId)
        if (activeCrate != null) {
            activeCrate.close()
            return true
        }
        return false
    }
}
