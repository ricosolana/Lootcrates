package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import com.crazicrafter1.nmsapi.NMSAPI;
import com.crazicrafter1.nmsapi.nbt.INBTTagCompound;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LootcratesAPI {
    public static Map<Class<? extends ILoot>, ItemStack> lootClasses = new HashMap<>();

    static void registerLoot(@Nonnull Class<? extends ILoot> lootClass) {
        lootClasses.put(lootClass, (ItemStack) ReflectionUtil.getFieldInstance(ReflectionUtil.getField(lootClass, "EDITOR_ICON"), null));
        ConfigurationSerialization.registerClass(lootClass, lootClass.getSimpleName());
        Main.get().notifier.info("Registering " + lootClass.getSimpleName());
    }

    @Nullable
    public static CrateSettings getCrateByID(@Nonnull String id) {
        return Main.get().rewardSettings.crates.get(id);
    }

    @Nullable
    public static CrateSettings getCrateFromItem(@Nullable final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;

        INBTTagCompound nbt = NMSAPI.getNBT(itemStack);
        if (nbt == null) return null;
        return getCrateByID(nbt.getString("Crate"));
    }

    @Nonnull
    public static ItemStack getCrateAsItem(@Nonnull final ItemStack itemStack, @Nonnull final String id) {
        Validate.notNull(id);

        INBTTagCompound nbt = NMSAPI.getOrCreateNBT(itemStack);
        nbt.setString("Crate", id);

        return nbt.setNBT(itemStack);
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

    public static boolean displayCrateMenu(@Nonnull Player p, @Nonnull String id, int lock_slot) {
        CrateSettings crate = getCrateByID(id);
        if (crate != null && !CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            new CrateInstance(p, crate, lock_slot).open();
            return true;
        }

        return false;
    }

    public static void displayCratePreview(@Nonnull Player p, @Nonnull String id) {
        CrateSettings crate = getCrateByID(id);
        if (crate != null)
            crate.getPreview().open(p);
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
