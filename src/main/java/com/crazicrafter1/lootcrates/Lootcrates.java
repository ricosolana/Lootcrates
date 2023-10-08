package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import com.crazicrafter1.lootcrates.crate.LootCollection;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Lootcrates {
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

    private static String CRATE_KEY = "Crate";
    private static String CRATE_CERT_KEY = "CrateCert";
    private static String TAG_KEY = "tag";

    private static LCMain PLUGIN = LCMain.get();

    // TODO I do not like how this loot set reduction and append is handled
    //  too unsafe and tacky
    @Deprecated
    public static boolean removeLootSet(String id, boolean failIfReferenced) {
        if (PLUGIN.rewardSettings.lootSets.size() > 1) {
            if (failIfReferenced) {
                for (Iterator<CrateSettings> it = getCrates(); it.hasNext(); ) {
                    CrateSettings crate = it.next();
                    // check for crates that use this loot set
                    // if used by any, then fail
                    if (crate.loot.get(id) != null)
                        return false;
                }
            }

            LootCollection lootCollection = PLUGIN.rewardSettings.lootSets.remove(id);
            if (lootCollection == null)
                return false;

            for (Iterator<CrateSettings> it = getCrates(); it.hasNext(); ) {
                CrateSettings crate = it.next();
                crate.loot.remove(id);
                if (crate.loot.getMap().isEmpty()) {
                    LootCollection next = PLUGIN.rewardSettings.lootSets.values().iterator().next();
                    crate.loot.add(next.id, 1);
                }
            }

            return true;
        }
        return false;
    }

    /**
     * Removes a lootSet by id (deletes it)
     * References will be removed from crates and replaced if loot is empty
     * @param id lootSet id
     * @return whether successful
     */
    public static boolean removeLootSet(String id) {
        if (PLUGIN.rewardSettings.lootSets.size() > 1) {
            LootCollection lootCollection = PLUGIN.rewardSettings.lootSets.remove(id);
            if (lootCollection == null)
                return false;

            for (Iterator<CrateSettings> it = getCrates(); it.hasNext(); ) {
                CrateSettings crate = it.next();
                crate.loot.remove(id);

                // if crate has no loot, add the next loot
                if (crate.loot.getMap().isEmpty()) {
                    LootCollection next = getLoot().next();
                    crate.loot.add(next.id, 1);
                }
            }

            return true;
        }
        return false;
    }

    public static CrateSettings createCrate(String id) {
        Map<String, Integer> map = new HashMap<>();
        map.put(getLoot().next().id, 1);
        return new CrateSettings(id, "select loot", 3, 4, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                map,
                ItemBuilder.mut(tagItemAsCrate(new ItemStack(Material.ENDER_CHEST), id)).name("my new crate").build(),
                CrateSettings.RevealType.GOOD_OL_DESTY
        );
    }

    public static void registerCrate(CrateSettings crateSettings) {
        PLUGIN.rewardSettings.crates.put(crateSettings.id, crateSettings);
    }

    @Nullable
    public static CrateSettings getCrate(@Nonnull String id) {
        return PLUGIN.rewardSettings.crates.get(id);
    }

    @Nullable
    public static CrateSettings getCrate(@Nullable final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;

        // item.nbt["tag"]["Crate"] (pseudocode)

        ReadWriteNBT tag = NBT.itemStackToNBT(itemStack).getCompound(TAG_KEY);
        if (tag == null) return null;
        return getCrate(tag.getString(CRATE_KEY));

        //INBTTagCompound nbt = NMSAPI.getNBT(itemStack);
        //if (nbt == null) return null;
        //return getCrate(nbt.getString("Crate"));
    }

    @Nonnull
    public static ItemStack tagItemAsCrate(@Nonnull final ItemStack itemStack, @Nonnull final String id) {
        Validate.notNull(id);

        ReadWriteNBT nbt = NBT.itemStackToNBT(itemStack);
        ReadWriteNBT tag = nbt.getOrCreateCompound(TAG_KEY);
        tag.setString(CRATE_KEY, id);
        return NBT.itemStackFromNBT(nbt);

        //INBTTagCompound nbt = NMSAPI.getOrCreateNBT(itemStack);
        //nbt.setString("Crate", id);
        //return nbt.setNBT(itemStack);
    }



    /**
     * Try to claim the UUID ticket attached to a crate (to prevent duplication)
     *  This can modify the legal ticket pool
     * @param itemStack crate item
     * @return The attached UUID or null if illegal
     */
    public static UUID claimTicket(ItemStack itemStack) {
        ReadWriteNBT tag = NBT.itemStackToNBT(itemStack).getCompound(TAG_KEY);
        if (tag == null) return null; // hmm
        UUID ticket = tag.getUUID(CRATE_CERT_KEY);
        return LCMain.crateCerts.remove(ticket) ? ticket : null;
    }

    /**
     * If the attacked ticket to the crate ItemStack can be claimed
     * @param itemStack crate item
     * @return The attached UUID or null if illegal
     */
    public static UUID canClaimTicket(ItemStack itemStack) {
        ReadWriteNBT tag = NBT.itemStackToNBT(itemStack).getCompound(TAG_KEY);
        if (tag == null) return null; // hmm
        UUID ticket = tag.getUUID(CRATE_CERT_KEY);
        return LCMain.crateCerts.contains(ticket) ? ticket : null;
    }

    public static Iterator<CrateSettings> getCrates() {
        return PLUGIN.rewardSettings.crates.values().iterator();
    }

    public static Iterator<LootCollection> getLoot() {
        return PLUGIN.rewardSettings.lootSets.values().iterator();
    }

    // TODO add force-or prevention check
    public static boolean showCrate(@Nonnull Player p, @Nonnull CrateSettings crate) {
        if (CrateInstance.CRATES.containsKey(p.getUniqueId()))
            return false;

        new CrateInstance(p, crate, null).open();
        return true;
    }

    public static boolean showPreview(@Nonnull Player p, @Nonnull CrateSettings crate) {
        if (CrateInstance.CRATES.containsKey(p.getUniqueId()))
            return false;

        crate.getPreview().open(p);
        return true;
    }

    /**
     * Closes a player-opened crate
     * @param p {@link Player} instance
     * @return whether the close was successful
     */
    public static boolean stopCrate(@Nonnull Player p) {
        CrateInstance activeCrate = CrateInstance.CRATES.remove(p.getUniqueId());
        if (activeCrate != null) {
            activeCrate.close();
            return true;
        }
        return false;
    }

}
