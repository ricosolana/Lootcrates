package com.crazicrafter1.lootcrates.editor.fireworks;

import com.crazicrafter1.crutils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FireworkWrapper {

    private final FireworkEffect effect;

    public FireworkWrapper(FireworkEffect effect) {
        this.effect = effect;
    }

    public ItemStack getStar() {
        StringBuilder lore = new StringBuilder();
        if (effect != null) {
            lore.append("&8Colors: \n");
            for (Color c : effect.getColors()) {
                lore.append(" &8- ");
                if (c.equals(Color.WHITE)) {
                    lore.append("&fwhite");
                } else if (c.equals(Color.SILVER)) {
                    lore.append("&7silver");
                } else if (c.equals(Color.GRAY)) {
                    lore.append("&8gray");
                } else if (c.equals(Color.BLACK)) {
                    lore.append("&0black");
                } else if (c.equals(Color.RED)) {
                    lore.append("&cred");
                } else if (c.equals(Color.MAROON)) {
                    lore.append("&4maroon");
                } else if (c.equals(Color.YELLOW)) {
                    lore.append("&eyellow");
                } else if (c.equals(Color.OLIVE)) {
                    lore.append("&2olive");
                } else if (c.equals(Color.LIME)) {
                    lore.append("&alime");
                } else if (c.equals(Color.GREEN)) {
                    lore.append("&agreen");
                } else if (c.equals(Color.AQUA)) {
                    lore.append("&baqua");
                } else if (c.equals(Color.TEAL)) {
                    lore.append("&3teal");
                } else if (c.equals(Color.BLUE)) {
                    lore.append("&9blue");
                } else if (c.equals(Color.NAVY)) {
                    lore.append("&1navy");
                } else if (c.equals(Color.FUCHSIA)) {
                    lore.append("&dfuchsia");
                } else if (c.equals(Color.PURPLE)) {
                    lore.append("&5purple");
                } else if (c.equals(Color.ORANGE)) {
                    lore.append("&6orange");
                } else {
                    lore.append(c.asRGB());
                }
                lore.append("\n");
            }

            lore.append("&8Fade colors: \n");
            for (Color c : effect.getFadeColors()) {
                lore.append(" &8- ");
                if (c.equals(Color.WHITE)) {
                    lore.append("&fwhite");
                } else if (c.equals(Color.SILVER)) {
                    lore.append("&7silver");
                } else if (c.equals(Color.GRAY)) {
                    lore.append("&8gray");
                } else if (c.equals(Color.BLACK)) {
                    lore.append("&0black");
                } else if (c.equals(Color.RED)) {
                    lore.append("&cred");
                } else if (c.equals(Color.MAROON)) {
                    lore.append("&4maroon");
                } else if (c.equals(Color.YELLOW)) {
                    lore.append("&eyellow");
                } else if (c.equals(Color.OLIVE)) {
                    lore.append("&2olive");
                } else if (c.equals(Color.LIME)) {
                    lore.append("&alime");
                } else if (c.equals(Color.GREEN)) {
                    lore.append("&agreen");
                } else if (c.equals(Color.AQUA)) {
                    lore.append("&baqua");
                } else if (c.equals(Color.TEAL)) {
                    lore.append("&3teal");
                } else if (c.equals(Color.BLUE)) {
                    lore.append("&9blue");
                } else if (c.equals(Color.NAVY)) {
                    lore.append("&1navy");
                } else if (c.equals(Color.FUCHSIA)) {
                    lore.append("&dfuchsia");
                } else if (c.equals(Color.PURPLE)) {
                    lore.append("&5purple");
                } else if (c.equals(Color.ORANGE)) {
                    lore.append("&6orange");
                } else {
                    lore.append(c.asRGB());
                }
                lore.append("\n");
            }

            lore.append("&8Flicker: ").append(effect.hasFlicker() ? "&2enabled" : "&cdisabled");
        }

        return new ItemBuilder(Material.FIREWORK_STAR).name("&eCurrent Effect").lore(lore.toString()).toItem();
    }

}
