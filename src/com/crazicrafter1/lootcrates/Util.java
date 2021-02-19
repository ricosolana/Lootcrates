package com.crazicrafter1.lootcrates;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

public class Util {


    public static boolean inRange(int i, int min, int max) {
        return (i <= max && i >= min);
    }

    private static HashSet<String> dyes = new HashSet<>(Arrays.asList("BLACK", "BLUE", "BROWN", "CYAN", "GRAY", "GREEN", "LIGHT_GRAY", "LIME", "MAGENTA", "ORANGE", "PINK", "PURPLE", "RED", "WHITE", "YELLOW"));

    public static ItemStack getDyeColoredItem(String name) {
        for (String dye : dyes) {

            if (name.contains(dye)) {

                // then get
                DyeColor dyeColor = DyeColor.valueOf(dye);

                String itemName = name.replaceAll(dye, "");

                if (itemName.startsWith("_")) itemName = itemName.replaceFirst("_", "");

                Material material = Material.matchMaterial(itemName);

                return new ItemStack(material, 1, dyeColor.getDyeData());

            }

        }

        return null;
    }

    public static void giveItemToPlayer(Player p, ItemStack item) {
        new BukkitRunnable() {
            @Override
            public void run() {

                Main.getInstance().debug("Gave item to player");

                if (!p.isDead()) {
                    HashMap<Integer, ItemStack> remaining = null;
                    if (!(remaining = p.getInventory().addItem(item)).isEmpty()) {
                        for (ItemStack itemStack : remaining.values()) {

                            //getPlayer().getWorld().dropItem(getPlayer().getLocation(), itemStack);
                            p.getWorld().dropItem(p.getLocation(), itemStack);

                        }
                        //ItemStack remaining =
                    }
                } else {

                    //getPlayer().getWorld().dropItem(getPlayer().getLocation(), itemStack);
                    p.getWorld().dropItem(p.getLocation(), item);

                    //ItemStack remaining =

                }
            }
        }.runTaskLater(Main.getInstance(), 1);
    }

    public static ItemStack getItem(String name, boolean useOldMethods) {

        if (!useOldMethods && name != null) {
            Material mat = Material.matchMaterial(name);
            if (mat != null)
                return new ItemStack(mat);
        } else {

            ItemStack item = getDyeColoredItem(name);

            if (item != null) {

                return item;

            } else {

                switch (name.toUpperCase()) {

                    case "EXPERIENCE_BOTTLE": return new ItemStack(Material.matchMaterial("EXP_BOTTLE"));
                    case "GOLDEN_APPLE": return new ItemStack(Material.GOLDEN_APPLE, 1, (short) 0);
                    case "ENCHANTED_GOLDEN_APPLE": return new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1);
                }

                return new ItemStack(Material.matchMaterial(name));
            }
        }

       return null;

    }

    public static Color matchColor(String c)
    {
        String color = c.toUpperCase();
        // BLUE, RED, WHITE, GRAY, GREEN, YELLOW, AQUA, BLACK, FUCHSIA, LIME, MAROON, NAVY, OLIVE
        // ORANGE, PURPLE, SILVER, TEAL
        if (color.equals("BLUE"))
            return Color.BLUE;
        if (color.equals("RED"))
            return Color.RED;
        if (color.equals("WHITE"))
            return Color.WHITE;
        if (color.equals("GRAY"))
            return Color.GRAY;
        if (color.equals("GREEN"))
            return Color.GREEN;
        if (color.equals("YELLOW"))
            return Color.YELLOW;
        if (color.equals("AQUA"))
            return Color.AQUA;
        if (color.equals("BLACK"))
            return Color.BLACK;
        if (color.equals("FUCHSIA"))
            return Color.FUCHSIA;
        if (color.equals("LIME"))
            return Color.LIME;
        if (color.equals("MAROON"))
            return Color.MAROON;
        if (color.equals("NAVY"))
            return Color.NAVY;
        if (color.equals("OLIVE"))
            return Color.OLIVE;
        if (color.equals("ORANGE"))
            return Color.ORANGE;
        if (color.equals("PURPLE"))
            return Color.PURPLE;
        if (color.equals("SILVER"))
            return Color.SILVER;
        if (color.equals("TEAL"))
            return Color.TEAL;
        return null;
    }

    public static void setName(ItemStack item, String name)
    {
        /*
        name
         */
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r"+name));
        //item.setItemMeta(meta);


        item.setItemMeta(meta);
    }

    public static void setLore(ItemStack item, List<String> lore)
    {
        ItemMeta meta = item.getItemMeta();

        for (int i = 0; i< lore.size(); i++)
        {
            lore.set(i, ChatColor.translateAlternateColorCodes('&', "&r"+ lore.get(i)));
        }

        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    public static int randomRange(int min, int max)
    {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    public static int randomRange(int min, int max, int min1, int max1)
    {
        if ((int)(Math.random()*2) == 0)
            return min + (int)(Math.random() * ((max - min) + 1));
        return min1 + (int)(Math.random() * ((max1 - min1) + 1));
    }

    //@Deprecated
    public static int randomRange(int min, int max, Random random)
    {
        return random.nextInt((max - min) + 1) + min;
    }

    // argument: float 0 -> 1
    public static boolean randomChance(float i)
    {
        return i >= Math.random();
    }

    public static boolean randomChance(float i, Random random)
    {
        return i <= (float)randomRange(0, 100, random) / 100f;
    }

    public static int sqDist(int x1, int y1, int x2, int y2) {
        return (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2);
    }

    public static int safeToInt(String s)
    {
        //if ()
        // test if is numeric

        if (isNumeric(s))
        {
            return Integer.parseInt(s);
        }
        return 0;
    }

    public static boolean isNumeric(String s)
    {
        for (int i=0;i<s.length();i++)
        {
            try {
                Integer.parseInt(s.substring(i));
            }
            catch(NumberFormatException e)
            {
                return false;
            }
        }
        return true;
    }

    public static Enchantment matchEnchant(String enchant)
    {
        String e = enchant.toUpperCase();

        if (e.equals("DURABILITY") || e.equals("UNBREAKING"))
            return Enchantment.DURABILITY;
        if (e.equals("ARROW_DAMAGE") || e.equals("POWER"))
            return Enchantment.ARROW_DAMAGE;
        if (e.equals("ARROW_FIRE") || e.equals("FLAME"))
            return Enchantment.ARROW_FIRE;
        if (e.equals("ARROW_INFINITE") || e.equals("INFINITY"))
            return Enchantment.ARROW_INFINITE;
        if (e.equals("ARROW_KNOCKBACK") || e.equals("PUNCH"))
            return Enchantment.ARROW_KNOCKBACK;
        if (e.equals("BINDING_CURSE") || e.equals("CURSE_OF_BINDING"))
            return Enchantment.BINDING_CURSE;
        if (e.equals("CHANELLING"))
            return Enchantment.CHANNELING;
        if (e.equals("DAMAGE_ALL") || e.equals("SHARPNESS"))
            return Enchantment.DAMAGE_ALL;
        if (e.equals("DAMAGE_ANTHROPODS") || e.equals("BANE_OF_ANTHROPODS"))
            return Enchantment.DAMAGE_ARTHROPODS;
        if (e.equals("DAMAGE_UNDEAD") || e.equals("SMITE"))
            return Enchantment.DAMAGE_UNDEAD;
        if (e.equals("DEPTH_STRIDER"))
            return Enchantment.DEPTH_STRIDER;
        if (e.equals("DIG_SPEED") || e.equals("EFFICIENCY"))
            return Enchantment.DIG_SPEED;
        if (e.equals("FIRE_ASPECT"))
            return Enchantment.FIRE_ASPECT;
        if (e.equals("FROST_WALKER"))
            return Enchantment.FROST_WALKER;
        if (e.equals("IMPALING"))
            return Enchantment.IMPALING;
        if (e.equals("KNOCKBACK"))
            return Enchantment.KNOCKBACK;
        if (e.equals("LOOT_BONUS_BLOCKS") || e.equals("FORTUNE"))
            return Enchantment.LOOT_BONUS_BLOCKS;
        if (e.equals("LOOT_BONUS_MOBS") || e.equals("LOOTING"))
            return Enchantment.LOOT_BONUS_MOBS;
        if (e.equals("LOYALTY"))
            return Enchantment.LOYALTY;
        if (e.equals("LUCK") || e.equals("LUCK_OF_THE_SEA"))
            return Enchantment.LUCK;
        if (e.equals("LURE"))
            return Enchantment.LURE;
        if (e.equals("MENDING"))
            return Enchantment.MENDING;
        if (e.equals("MULTISHOT"))
            return Enchantment.MULTISHOT;
        if (e.equals("OXYGEN") || e.equals("RESPIRATION"))
            return Enchantment.OXYGEN;
        if (e.equals("PIERCING"))
            return Enchantment.PIERCING;
        if (e.equals("PROTECTION_ENVIRONMENTAL") || e.equals("PROTECTION"))
            return Enchantment.PROTECTION_ENVIRONMENTAL;
        if (e.equals("PROTECTION_FIRE") || e.equals("FIRE_PROTECTION"))
            return Enchantment.PROTECTION_FIRE;
        if (e.equals("PROTECTION_FALL") || e.equals("FEATHER_FALLING"))
            return  Enchantment.PROTECTION_FALL;
        if (e.equals("PROTECTION_EXPLOSIONS") || e.equals("BLAST_PROTECTION"))
            return Enchantment.PROTECTION_EXPLOSIONS;
        if (e.equals("PROTECTION_PROJECTILE") || e.equals("PROJECTILE_PROTECTION"))
            return Enchantment.PROTECTION_PROJECTILE;
        if (e.equals("QUICK_CHARGE"))
            return Enchantment.QUICK_CHARGE;
        if (e.equals("RIPTIDE"))
            return Enchantment.RIPTIDE;
        if (e.equals("SILK_TOUCH"))
            return Enchantment.SILK_TOUCH;
        if (e.equals("SWEEPING_EDGE"))
            return Enchantment.SWEEPING_EDGE;
        if (e.equals("THORNS"))
            return Enchantment.THORNS;
        if (e.equals("VANISHING_CURSE") || e.equals("CURSE_OF_VANISHING"))
            return Enchantment.VANISHING_CURSE;
        if (e.equals("WATER_WORKER") || e.equals("AQUA_AFFINITY"))
            return Enchantment.WATER_WORKER;
        return null;
    }

    static void downloadURLAsFile(String link, String out) {
        try {
            URL website = new URL(link);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(out);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            //fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
