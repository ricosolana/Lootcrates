package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.refl.ItemStackMirror;
import com.crazicrafter1.crutils.refl.NBTTagCompoundMirror;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LootCratesAPI {

    //static Set<Class<? extends ILoot>> lootClasses = new HashSet<>();
    public static Map<Class<? extends ILoot>, ItemStack> lootClasses = new HashMap<>();

    //@Deprecated
    //static void registerLoot(Class<? extends ILoot> lootClass, String alias) {
    //    registerLoot(lootClass,
    //            ItemBuilder.copyOf(Material.GOLD_INGOT)
    //                    .name(lootClass.getSimpleName())
    //                    .build(),
    //            alias);
    //}

    //@Deprecated
    //static void registerLoot(Class<? extends ILoot> lootClass, ItemStack itemStack, String alias) {
    //    lootClasses.put(lootClass, itemStack);
    //    ConfigurationSerialization.registerClass(lootClass, alias);
    //}

    //static void registerLoot(Class<? extends ILoot> lootClass) {
    //    registerLoot(lootClass,
    //            ItemBuilder.copyOf(Material.GOLD_INGOT)
    //                    .name(lootClass.getSimpleName())
    //                    .build());
    //}

    static void registerLoot(Class<? extends ILoot> lootClass) {
        lootClasses.put(lootClass, (ItemStack) ReflectionUtil.getFieldInstance(ReflectionUtil.getField(lootClass, "EDITOR_ICON"), null));
        ConfigurationSerialization.registerClass(lootClass, lootClass.getSimpleName());
        Main.get().info("Registering " + lootClass.getSimpleName());
    }

    public static Crate getCrateByID(String id) {
        return Main.get().data.crates.get(id);
    }

    public static Crate extractCrateFromItem(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;

        ItemStackMirror nmsStack = new ItemStackMirror(itemStack);

        NBTTagCompoundMirror nbt = nmsStack.getTag();

        if (nbt == null)
            return null;

        return getCrateByID(nbt.getString("Crate"));
    }

    public static ItemStack makeCrate(final ItemStack itemStack, final String crate) {
        ItemStackMirror nmsStack = new ItemStackMirror(itemStack);

        NBTTagCompoundMirror nbt = nmsStack.getOrCreateTag();
        nbt.setString("Crate", crate);
        nmsStack.setTag(nbt);

        Main.get().info("Crate: " +
            nbt.getString("Crate"));

        return nmsStack.getItemStack();
    }

    /**
     * Opens a crate by id to a player
     */
    public static boolean openCrate(Player p, String name, int lock_slot) {
        Crate crate = getCrateByID(name);
        if (crate != null) {
            Main.get().openCrates.put(p.getUniqueId(),
                    new ActiveCrate(p, crate, lock_slot));
            return true;
        }

        return false;
    }

    /**
     * Close a crate to a player
     */
    public static void closeCrate(Player p) {
        Main.get().openCrates.remove(p.getUniqueId()).close();
    }

}
