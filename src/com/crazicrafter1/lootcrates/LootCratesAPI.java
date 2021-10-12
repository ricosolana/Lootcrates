package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.refl.ItemStackMirror;
import com.crazicrafter1.crutils.refl.NBTTagCompoundMirror;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.loot.AbstractLoot;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class LootCratesAPI {

    static Set<Class<? extends AbstractLoot>> lootClasses = new HashSet<>();

    public static void registerLoot(Class<? extends AbstractLoot> lootClass, String alias) {
        lootClasses.add(lootClass);
        ConfigurationSerialization.registerClass(lootClass, alias);
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
