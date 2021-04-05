package com.crazicrafter1.lootcrates.util;

import com.crazicrafter1.lootcrates.Main;
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

    private static HashSet<String> dyes = new HashSet<>(Arrays.asList("BLACK", "BLUE", "BROWN", "CYAN", "GRAY", "GREEN", "LIGHT_GRAY", "LIME", "MAGENTA", "ORANGE", "PINK", "PURPLE", "RED", "WHITE", "YELLOW"));

    private static ItemStack getDyeColoredItem(String name) {
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

    public static ItemStack getCompatibleItem(String name) {

        if (name == null)
            return null;

        name = name.toUpperCase();

        // Modern
        Material mat = Material.matchMaterial(name);
        if (mat != null)
            return new ItemStack(mat);

        ItemStack item = getDyeColoredItem(name);

        if (item != null)
            return item;

        switch (name) {
            case "EXPERIENCE_BOTTLE":
                return new ItemStack(Material.matchMaterial("EXP_BOTTLE"));
            case "GOLDEN_APPLE":
                return new ItemStack(Material.GOLDEN_APPLE, 1, (short) 0);
            case "ENCHANTED_GOLDEN_APPLE":
                return new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1);
        }

        return null;
    }

    public static Color matchColor(String color)
    {
        // BLUE, RED, WHITE, GRAY, GREEN, YELLOW, AQUA, BLACK, FUCHSIA, LIME, MAROON, NAVY, OLIVE
        // ORANGE, PURPLE, SILVER, TEAL
        switch (color.toUpperCase()) {
            case "BLUE":
                return Color.BLUE;
            case "RED":
                return Color.RED;
            case "WHITE":
                return Color.WHITE;
            case "GRAY":
                return Color.GRAY;
            case "GREEN":
                return Color.GREEN;
            case "YELLOW":
                return Color.YELLOW;
            case "AQUA":
                return Color.AQUA;
            case "BLACK":
                return Color.BLACK;
            case "FUCHSIA":
                return Color.FUCHSIA;
            case "LIME":
                return Color.LIME;
            case "MAROON":
                return Color.MAROON;
            case "NAVY":
                return Color.NAVY;
            case "OLIVE":
                return Color.OLIVE;
            case "ORANGE":
                return Color.ORANGE;
            case "PURPLE":
                return Color.PURPLE;
            case "SILVER":
                return Color.SILVER;
            case "TEAL":
                return Color.TEAL;
            default:
                return null;
        }
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
    @Deprecated
    public static boolean randomChance(float i)
    {
        return i >= Math.random();
    }

    @Deprecated
    public static boolean randomChance(float i, Random random)
    {
        return i <= (float)randomRange(0, 100, random) / 100f;
    }

    public static int sqDist(int x1, int y1, int x2, int y2) {
        return (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2);
    }

    public static boolean toInt(String s, IntegerC wrapper)
    {
        try {
            wrapper.value = Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Enchantment matchEnchant(String enchant)
    {
        String e = enchant.toUpperCase().replaceAll(" ", "_");

        switch (e) {
            case "ARROW_DAMAGE":
            case "POWER":
                return Enchantment.ARROW_DAMAGE;
            case "ARROW_FIRE":
            case "FLAME":
                return Enchantment.ARROW_FIRE;
            case "ARROW_INFINITE":
            case "INFINITY":
                return Enchantment.ARROW_INFINITE;
            case "ARROW_KNOCKBACK":
            case "PUNCH":
                return Enchantment.ARROW_KNOCKBACK;
            case "BINDING_CURSE":
            case "CURSE_OF_BINDING":
                return Enchantment.BINDING_CURSE;
            case "CHANNELING":
                return Enchantment.CHANNELING;
            case "DAMAGE_ALL":
            case "SHARPNESS":
                return Enchantment.DAMAGE_ALL;
            case "DAMAGE_ANTHROPODS":
            case "BANE_OF_ANTHROPODS":
                return Enchantment.DAMAGE_ARTHROPODS;
            case "DAMAGE_UNDEAD":
            case "SMITE":
                return Enchantment.DAMAGE_UNDEAD;
            case "DEPTH_STRIDER":
                return Enchantment.DEPTH_STRIDER;
            case "DIG_SPEED":
            case "EFFICIENCY":
                return Enchantment.DIG_SPEED;
            case "DURABILITY":
            case "UNBREAKING":
                return Enchantment.DURABILITY;
            case "FIRE_ASPECT":
                return Enchantment.FIRE_ASPECT;
            case "FROST_WALKER":
                return Enchantment.FROST_WALKER;
            case "IMPALING":
                return Enchantment.IMPALING;
            case "KNOCKBACK":
                return Enchantment.KNOCKBACK;
            case "LOOT_BONUS_BLOCKS":
            case "FORTUNE":
                return Enchantment.LOOT_BONUS_BLOCKS;
            case "LOOT_BONUS_MOBS":
            case "LOOTING":
                return Enchantment.LOOT_BONUS_MOBS;
            case "LOYALTY":
                return Enchantment.LOYALTY;
            case "LUCK":
            case "LUCK_OF_THE_SEA":
                return Enchantment.LUCK;
            case "LURE":
                return Enchantment.LURE;
            case "MENDING":
                return Enchantment.MENDING;
            case "MULTISHOT":
                return Enchantment.MULTISHOT;
            case "OXYGEN":
            case "RESPIRATION":
                return Enchantment.OXYGEN;
            case "PIERCING":
                return Enchantment.PIERCING;
            case "PROTECTION_ENVIRONMENTAL":
            case "PROTECTION":
                return Enchantment.PROTECTION_ENVIRONMENTAL;
            case "PROTECTION_FIRE":
            case "FIRE_PROTECTION":
                return Enchantment.PROTECTION_FIRE;
            case "PROTECTION_FALL":
            case "FEATHER_FALLING":
                return Enchantment.PROTECTION_FALL;
            case "PROTECTION_EXPLOSIONS":
            case "BLAST_PROTECTION":
                return Enchantment.PROTECTION_EXPLOSIONS;
            case "PROTECTION_PROJECTILE":
            case "PROJECTILE_PROTECTION":
                return Enchantment.PROTECTION_PROJECTILE;
            case "QUICK_CHARGE":
                return Enchantment.QUICK_CHARGE;
            case "RIPTIDE":
                return Enchantment.RIPTIDE;
            case "SILK_TOUCH":
                return Enchantment.SILK_TOUCH;
            case "SOUL_SPEED":
                return Enchantment.SOUL_SPEED;
            case "SWEEPING_EDGE":
                return Enchantment.SWEEPING_EDGE;
            case "THORNS":
                return Enchantment.THORNS;
            case "VANISHING_CURSE":
            case "CURSE_OF_VANISHING":
                return Enchantment.VANISHING_CURSE;
            case "WATER_WORKER":
            case "AQUA_AFFINITY":
                return Enchantment.WATER_WORKER;
            default:
                return null;
        }
    }

    /*
        can this be trusted?
     */
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
