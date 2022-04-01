package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
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

public class LootCratesAPI {
    public static Map<Class<? extends ILoot>, ItemStack> lootClasses = new HashMap<>();

    static void registerLoot(@Nonnull Class<? extends ILoot> lootClass) {
        lootClasses.put(lootClass, (ItemStack) ReflectionUtil.getFieldInstance(ReflectionUtil.getField(lootClass, "EDITOR_ICON"), null));
        ConfigurationSerialization.registerClass(lootClass, lootClass.getSimpleName());
        Main.get().info("Registering " + lootClass.getSimpleName());
    }

    @Nullable
    public static Crate getCrateByID(@Nonnull String id) {
        return Main.get().data.crates.get(id);
    }

    @Nullable
    public static Crate extractCrateFromItem(@Nullable final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;

        INBTTagCompound nbt = NMSAPI.getNBT(itemStack);
        if (nbt == null) return null;
        return getCrateByID(nbt.getString("Crate"));

        //ItemStackMirror nmsStack = new ItemStackMirror(itemStack);
        //NBTTagCompoundMirror nbt = nmsStack.getTag();
        //if (nbt == null) return null;
        //return getCrateByID(nbt.getString("Crate"));
    }

    @Nonnull
    public static ItemStack makeCrate(@Nonnull final ItemStack itemStack, @Nonnull final String id) {
        Validate.notNull(id);

        INBTTagCompound nbt = NMSAPI.getOrCreateNBT(itemStack);
        nbt.setString("Crate", id);
        return nbt.setNBT(itemStack);

        //ItemStackMirror nmsStack = new ItemStackMirror(itemStack);
        //NBTTagCompoundMirror nbt = nmsStack.getOrCreateTag();
        //nbt.setString("Crate", id);
        //return nmsStack.getItemStack();
    }

    /**
     * Show a crate to a player
     * @param p {@link Player} instance
     * @param id crate id
     * @return whether the open was successful
     */
    public static boolean openCrate(@Nonnull Player p, @Nonnull String id) {
        return openCrate(p, id, -1);
    }

    public static boolean openCrate(@Nonnull Player p, @Nonnull String id, int lock_slot) {
        Crate crate = getCrateByID(id);
        if (crate != null && !Main.get().openCrates.containsKey(p.getUniqueId())) {
            new ActiveCrate(p, crate, lock_slot).open();
            return true;
        }

        return false;
    }

    /**
     * Close a player currently opened crate
     * @param p {@link Player} instance
     * @return whether the close was successful
     */
    public static boolean closeCrate(@Nonnull Player p) {
        ActiveCrate activeCrate = Main.get().openCrates.remove(p.getUniqueId());
        if (activeCrate != null) {
            activeCrate.close();
            return true;
        }
        return false;
    }

}
