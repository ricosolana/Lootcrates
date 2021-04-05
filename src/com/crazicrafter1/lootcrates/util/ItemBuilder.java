package com.crazicrafter1.lootcrates.util;

//import net.minecraft.server.v1_14_R1.*;
import com.crazicrafter1.lootcrates.util.refl.GameProfileMirror;
import com.crazicrafter1.lootcrates.util.refl.PropertyMirror;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
//import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    private ItemStack itemStack;

    private ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    private ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static ItemBuilder builder(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder builder(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    // lexicals
    public ItemBuilder mergeLexicals(ItemStack itemStack) {
        return this.name(itemStack.getItemMeta().getDisplayName()).lore(itemStack.getItemMeta().getLore());
    }

    private static Object makeProfile(String b64) {
        // random uuid based on the b64 string
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );

        // https://github.com/deanveloper/SkullCreator/blob/master/src/main/java/dev/dbassett/skullcreator/SkullCreator.java#L260

        GameProfileMirror profile = new GameProfileMirror(id, "aaaaa");
        profile.putProperty("textures", new PropertyMirror("textures", b64, null));
        return profile.getInstance();

        //GameProfile profile = new GameProfile(id, "aaaaa");
        //profile.getProperties().put("textures", new Property("textures", b64));
        //return profile;
    }


    public ItemBuilder skull(String base64) {
        if (base64 == null)
            return this;

        SkullMeta meta = (SkullMeta)itemStack.getItemMeta();

        Method setProfileMethod = ReflectionUtil.getMethod(
                ReflectionUtil.getCraftClass("inventory.CraftMetaSkull"),
                "setProfile",
                GameProfileMirror.gameProfileClass);

        ReflectionUtil.invokeMethod(setProfileMethod, meta, makeProfile(base64));

        itemStack.setItemMeta(meta);

        return this;
    }

    /**
        Set the custom model data of the item to work with a texture pack
     @param i the id of the texture
     */
    public ItemBuilder customModelData(Integer i) {
        if (i == null)
            return this;

        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(i);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
        Set the custom display name of an item
     @param name the name with &codes
     */
    public ItemBuilder name(String name) {
        if (name == null)
            return this;

        ItemMeta meta = itemStack.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r"+name));
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
        Set the lore of an item
     @param lore the lore
     */
    public ItemBuilder lore(String[] lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        if (lore == null)
            return this;

        ItemMeta meta = itemStack.getItemMeta();

        for (int i=0; i<lore.size(); i++)
            lore.set(i, ChatColor.translateAlternateColorCodes('&', "&r"+lore.get(i)));

        meta.setLore(lore);

        itemStack.setItemMeta(meta);
        return this;
    }

    /**
       Set the color of leather armor or a potion
     @param r red
     @param g green
     @param b blue
     */
    public ItemBuilder color(int r, int g, int b) {
        return this.color(Color.fromRGB(r, g, b));
    }

    public ItemBuilder color(Color color) {
        if (color == null)
            return this;

        ItemMeta meta = itemStack.getItemMeta();

        if (meta instanceof PotionMeta) {
            ((PotionMeta)meta).setColor(color);
            itemStack.setItemMeta(meta);
        } else if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta)meta).setColor(color);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
       Flag an item as unbreakable
     */
    public ItemBuilder unbreakable() {
        ItemMeta meta = itemStack.getItemMeta();

        if (!(meta instanceof Damageable))
            return this;

        meta.setUnbreakable(true);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder enchant(Enchantment e, int level) {
        itemStack.addUnsafeEnchantment(e, level);

        return this;
    }

    public ItemBuilder hideFlags(ItemFlag ... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(flags);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder glow(boolean state){
        if (!state) return this;

        itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 0);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        return this;
    }

    /*
    public ItemBuilder fast(){
        ItemStack item = new ItemStack(itemStack);

        net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        NBTTagCompound nbt = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();

        NBTTagList nbtTags = new NBTTagList();
        NBTTagCompound speed = new NBTTagCompound();

        speed.set("AttributeName", new NBTTagString("generic.attackSpeed"));
        speed.set("Name", new NBTTagString("Blah"));
        speed.set("Amount", new NBTTagDouble(9.8));
        speed.set("Operation", new NBTTagInt(0));
        speed.set("UUIDLeast", new NBTTagInt(1));
        speed.set("UUIDMost", new NBTTagInt(1));

        nbtTags.add(speed);
        nbt.set("AttributeModifiers", nbtTags);
        nmsStack.setTag(nbt);
        return new ItemBuilder(CraftItemStack.asCraftMirror(nmsStack));
    }
     */

    public ItemStack toItem() {
        return itemStack;
    }

}
