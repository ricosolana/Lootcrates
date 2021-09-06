package com.crazicrafter1.lootcrates.util;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
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
                if (p.isDead()) {
                    p.getWorld().dropItem(p.getLocation(), item);
                } else {
                    HashMap<Integer, ItemStack> remaining;
                    if (!(remaining = p.getInventory().addItem(item)).isEmpty()) {
                        for (ItemStack itemStack : remaining.values()) {
                            p.getWorld().dropItem(p.getLocation(), itemStack);
                        }
                    }
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

        return switch (name) {
            case "EXPERIENCE_BOTTLE" -> new ItemStack(Material.matchMaterial("EXP_BOTTLE"));
            case "GOLDEN_APPLE" -> new ItemStack(Material.GOLDEN_APPLE, 1, (short) 0);
            case "ENCHANTED_GOLDEN_APPLE" -> new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1);
            default -> null;
        };

    }

    public static Color matchColor(String color) {
        // Java 16 magic!
        return switch (color.toUpperCase()) {
            case "BLUE" -> Color.BLUE;
            case "RED" -> Color.RED;
            case "WHITE" -> Color.WHITE;
            case "GRAY" -> Color.GRAY;
            case "GREEN" -> Color.GREEN;
            case "YELLOW" -> Color.YELLOW;
            case "AQUA" -> Color.AQUA;
            case "BLACK" -> Color.BLACK;
            case "FUCHSIA" -> Color.FUCHSIA;
            case "LIME" -> Color.LIME;
            case "MAROON" -> Color.MAROON;
            case "NAVY" -> Color.NAVY;
            case "OLIVE" -> Color.OLIVE;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            case "SILVER" -> Color.SILVER;
            case "TEAL" -> Color.TEAL;
            default -> null;
        };
    }

    public static int randomRange(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    public static int randomRange(int min, int max, int min1, int max1) {
        if ((int)(Math.random()*2) == 0)
            return min + (int)(Math.random() * ((max - min) + 1));
        return min1 + (int)(Math.random() * ((max1 - min1) + 1));
    }

    public static int randomRange(int min, int max, Random random)
    {
        return random.nextInt((max - min) + 1) + min;
    }

    /**
     * Returns a chance
     * @param i [0, 1]
     * @return whether 'i' exceeded a chance
     */
    public static boolean randomChance(float i) {
        return i >= Math.random();
    }

    @Deprecated
    public static boolean randomChance(float i, Random random) {
        return i <= (float)randomRange(0, 100, random) / 100f;
    }

    public static int sqDist(int x1, int y1, int x2, int y2) {
        return (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2);
    }

    public static boolean toInt(String s, Int wrapper) {
        try {
            wrapper.value = Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Enchantment matchEnchant(String enchant) {
        String e = enchant.toUpperCase().replaceAll(" ", "_");

        return switch (e) {
            case "ARROW_DAMAGE", "POWER" -> Enchantment.ARROW_DAMAGE;
            case "ARROW_FIRE", "FLAME" -> Enchantment.ARROW_FIRE;
            case "ARROW_INFINITE", "INFINITY" -> Enchantment.ARROW_INFINITE;
            case "ARROW_KNOCKBACK", "PUNCH" -> Enchantment.ARROW_KNOCKBACK;
            case "BINDING_CURSE", "CURSE_OF_BINDING" -> Enchantment.BINDING_CURSE;
            case "CHANNELING" -> Enchantment.CHANNELING;
            case "DAMAGE_ALL", "SHARPNESS" -> Enchantment.DAMAGE_ALL;
            case "DAMAGE_ANTHROPODS", "BANE_OF_ANTHROPODS" -> Enchantment.DAMAGE_ARTHROPODS;
            case "DAMAGE_UNDEAD", "SMITE" -> Enchantment.DAMAGE_UNDEAD;
            case "DEPTH_STRIDER" -> Enchantment.DEPTH_STRIDER;
            case "DIG_SPEED", "EFFICIENCY" -> Enchantment.DIG_SPEED;
            case "DURABILITY", "UNBREAKING" -> Enchantment.DURABILITY;
            case "FIRE_ASPECT" -> Enchantment.FIRE_ASPECT;
            case "FROST_WALKER" -> Enchantment.FROST_WALKER;
            case "IMPALING" -> Enchantment.IMPALING;
            case "KNOCKBACK" -> Enchantment.KNOCKBACK;
            case "LOOT_BONUS_BLOCKS", "FORTUNE" -> Enchantment.LOOT_BONUS_BLOCKS;
            case "LOOT_BONUS_MOBS", "LOOTING" -> Enchantment.LOOT_BONUS_MOBS;
            case "LOYALTY" -> Enchantment.LOYALTY;
            case "LUCK", "LUCK_OF_THE_SEA" -> Enchantment.LUCK;
            case "LURE" -> Enchantment.LURE;
            case "MENDING" -> Enchantment.MENDING;
            case "MULTISHOT" -> Enchantment.MULTISHOT;
            case "OXYGEN", "RESPIRATION" -> Enchantment.OXYGEN;
            case "PIERCING" -> Enchantment.PIERCING;
            case "PROTECTION_ENVIRONMENTAL", "PROTECTION" -> Enchantment.PROTECTION_ENVIRONMENTAL;
            case "PROTECTION_FIRE", "FIRE_PROTECTION" -> Enchantment.PROTECTION_FIRE;
            case "PROTECTION_FALL", "FEATHER_FALLING" -> Enchantment.PROTECTION_FALL;
            case "PROTECTION_EXPLOSIONS", "BLAST_PROTECTION" -> Enchantment.PROTECTION_EXPLOSIONS;
            case "PROTECTION_PROJECTILE", "PROJECTILE_PROTECTION" -> Enchantment.PROTECTION_PROJECTILE;
            case "QUICK_CHARGE" -> Enchantment.QUICK_CHARGE;
            case "RIPTIDE" -> Enchantment.RIPTIDE;
            case "SILK_TOUCH" -> Enchantment.SILK_TOUCH;
            case "SOUL_SPEED" -> Enchantment.SOUL_SPEED;
            case "SWEEPING_EDGE" -> Enchantment.SWEEPING_EDGE;
            case "THORNS" -> Enchantment.THORNS;
            case "VANISHING_CURSE", "CURSE_OF_VANISHING" -> Enchantment.VANISHING_CURSE;
            case "WATER_WORKER", "AQUA_AFFINITY" -> Enchantment.WATER_WORKER;
            default -> null;
        };
    }

    public static PotionEffectType matchPotionEffectType(String effect) {
        String e = effect.toUpperCase().replaceAll(" ", "_");

        return switch (e) {
            case "ABSORPTION" -> PotionEffectType.ABSORPTION;
            case "BAD_OMEN" -> PotionEffectType.BAD_OMEN;
            case "BLINDNESS" -> PotionEffectType.BLINDNESS;
            case "CONDUIT_POWER" -> PotionEffectType.CONDUIT_POWER;
            case "CONFUSION", "NAUSEA" -> PotionEffectType.CONFUSION;
            case "DAMAGE_RESISTANCE", "RESISTANCE" -> PotionEffectType.DAMAGE_RESISTANCE;
            case "DOLPHINS_GRACE" -> PotionEffectType.DOLPHINS_GRACE;
            case "FAST_DIGGING", "HASTE" -> PotionEffectType.FAST_DIGGING;
            case "FIRE_RESISTANCE" -> PotionEffectType.FIRE_RESISTANCE;
            case "GLOWING" -> PotionEffectType.GLOWING;
            case "HARM", "INSTANT_DAMAGE" -> PotionEffectType.HARM;
            case "HEAL", "INSTANT_HEALTH" -> PotionEffectType.HEAL;
            case "HEALTH_BOOST" -> PotionEffectType.HEALTH_BOOST;
            case "HERO_OF_THE_VILLAGE" -> PotionEffectType.HERO_OF_THE_VILLAGE;
            case "HUNGER" -> PotionEffectType.HUNGER;
            case "INCREASE_DAMAGE", "STRENGTH" -> PotionEffectType.INCREASE_DAMAGE;
            case "INVISIBILITY" -> PotionEffectType.INVISIBILITY;
            case "JUMP", "JUMP_BOOST" -> PotionEffectType.JUMP;
            case "LEVITATION" -> PotionEffectType.LEVITATION;
            case "LUCK" -> PotionEffectType.LUCK;
            case "NIGHT_VISION" -> PotionEffectType.NIGHT_VISION;
            case "POISON" -> PotionEffectType.POISON;
            case "REGENERATION" -> PotionEffectType.REGENERATION;
            case "SATURATION" -> PotionEffectType.SATURATION;
            case "SLOW", "SLOWNESS" -> PotionEffectType.SLOW;
            case "SLOW_DIGGING", "MINING_FATIGUE" -> PotionEffectType.SLOW_DIGGING;
            case "SLOW_FALLING" -> PotionEffectType.SLOW_FALLING;
            case "SPEED", "SWIFTNESS" -> PotionEffectType.SPEED;
            case "UNLUCK", "BAD_LUCK" -> PotionEffectType.UNLUCK;
            case "WATER_BREATHING" -> PotionEffectType.WATER_BREATHING;
            case "WEAKNESS" -> PotionEffectType.WEAKNESS;
            case "WITHER" -> PotionEffectType.WITHER;
            default -> null;
        };
    }

    // Needs further testing
    @Deprecated
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
