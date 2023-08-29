package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import com.crazicrafter1.lootcrates.crate.LootCollection;
import com.google.common.collect.ImmutableMap;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

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

    // TODO I do not like how this loot set reduction and append is handled
    //  too unsafe and tacky
    @Deprecated
    public static boolean removeLootSet(String id, boolean failIfReferenced) {
        if (LCMain.get().rewardSettings.lootSets.size() > 1) {
            if (failIfReferenced) {
                for (Iterator<CrateSettings> it = getCrates(); it.hasNext(); ) {
                    CrateSettings crate = it.next();
                    // check for crates that use this loot set
                    // if used by any, then fail
                    if (crate.loot.get(id) != null)
                        return false;
                }
            }

            LootCollection lootCollection = LCMain.get().rewardSettings.lootSets.remove(id);
            if (lootCollection == null)
                return false;

            for (Iterator<CrateSettings> it = getCrates(); it.hasNext(); ) {
                CrateSettings crate = it.next();
                crate.loot.remove(id);
                if (crate.loot.getMap().isEmpty()) {
                    LootCollection next = LCMain.get().rewardSettings.lootSets.values().iterator().next();
                    crate.loot.add(next.id, 1);
                }

                //crate.removeLootSet(lootSetSettings.id);
                // crates should directly refer
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
        if (LCMain.get().rewardSettings.lootSets.size() > 1) {
            LootCollection lootCollection = LCMain.get().rewardSettings.lootSets.remove(id);
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
        return new CrateSettings(id, "select loot", 3, 4, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                ImmutableMap.of(getLoot().next().id, 1),
                ItemBuilder.mut(tagItemAsCrate(new ItemStack(Material.ENDER_CHEST), id)).name("my new crate").build(),
                CrateSettings.RevealType.GOOD_OL_DESTY
        );
    }

    public static void registerCrate(CrateSettings crateSettings) {
        LCMain.get().rewardSettings.crates.put(crateSettings.id, crateSettings);
    }

    @Nullable
    public static CrateSettings getCrate(@Nonnull String id) {
        return LCMain.get().rewardSettings.crates.get(id);
    }

    @Nullable
    public static CrateSettings getCrate(@Nullable final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;

        // item.nbt["tag"]["Crate"] (pseudocode)

        ReadWriteNBT tag = NBT.itemStackToNBT(itemStack).getCompound("tag");
        if (tag == null) return null;
        return getCrate(tag.getString("Crate"));

        //INBTTagCompound nbt = NMSAPI.getNBT(itemStack);
        //if (nbt == null) return null;
        //return getCrate(nbt.getString("Crate"));
    }

    @Nonnull
    public static ItemStack tagItemAsCrate(@Nonnull final ItemStack itemStack, @Nonnull final String id) {
        Validate.notNull(id);

        ReadWriteNBT nbt = NBT.itemStackToNBT(itemStack);
        ReadWriteNBT tag = nbt.getCompound("tag");
        tag.setString("Crate", id);
        return NBT.itemStackFromNBT(nbt);

        //INBTTagCompound nbt = NMSAPI.getOrCreateNBT(itemStack);
        //nbt.setString("Crate", id);
        //return nbt.setNBT(itemStack);
    }

    public static Iterator<CrateSettings> getCrates() {
        return LCMain.get().rewardSettings.crates.values().iterator();
    }

    public static Iterator<LootCollection> getLoot() {
        return LCMain.get().rewardSettings.lootSets.values().iterator();
    }

    /**
     * Show a crate to a player
     * @param p {@link Player} instance
     * @param id crate id
     * @return whether the open was successful
     */
    @Deprecated
    public static boolean displayCrateMenu(@Nonnull Player p, @Nonnull String id) {
        return displayCrateMenu(p, id, -1);
    }

    @Deprecated
    public static boolean displayCrateMenu(@Nonnull Player p, @Nonnull String id, int lock_slot) {
        CrateSettings crate = getCrate(id);
        if (crate != null && !CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            new CrateInstance(p, crate, lock_slot).open();
            return true;
        }

        return false;
    }

    public static boolean displayCrateMenu(@Nonnull Player p, @Nonnull CrateSettings crate, int lock_slot) {
        if (CrateInstance.CRATES.containsKey(p.getUniqueId()))
            return false;

        new CrateInstance(p, crate, lock_slot).open();
        return true;
    }

    public static boolean displayCratePreview(@Nonnull Player p, @Nonnull CrateSettings crate) {
        if (CrateInstance.CRATES.containsKey(p.getUniqueId()))
            return false;

        crate.getPreview().open(p);
        return true;
    }

    /**
     * Close a player currently opened crate
     * @param p {@link Player} instance
     * @return whether the close was successful
     */
    public static boolean endDisplayCrateMenu(@Nonnull Player p) {
        CrateInstance activeCrate = CrateInstance.CRATES.remove(p.getUniqueId());
        if (activeCrate != null) {
            activeCrate.close();
            return true;
        }
        return false;
    }

}
