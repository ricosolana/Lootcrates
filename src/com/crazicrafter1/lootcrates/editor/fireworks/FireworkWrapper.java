package com.crazicrafter1.lootcrates.editor.fireworks;

import com.crazicrafter1.crutils.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FireworkWrapper {

    private FireworkEffect effect;

    public FireworkWrapper(FireworkEffect effect) {
        this.effect = effect;
    }

    public ItemStack getStar() {
        StringBuilder lore = new StringBuilder();
        if (effect != null) {
            lore.append("Colors: \n");
            for (Color c : effect.getColors()) {
                lore.append(" - ");
                if (c.equals(Color.WHITE)) {
                    lore.append("white");
                } else if (c.equals(Color.SILVER)) {
                    lore.append("silver");
                } else if (c.equals(Color.GRAY)) {
                    lore.append("gray");
                } else if (c.equals(Color.BLACK)) {
                    lore.append("black");
                } else if (c.equals(Color.RED)) {
                    lore.append("red");
                } else if (c.equals(Color.MAROON)) {
                    lore.append("maroon");
                } else if (c.equals(Color.YELLOW)) {
                    lore.append("yellow");
                } else if (c.equals(Color.OLIVE)) {
                    lore.append("olive");
                } else if (c.equals(Color.LIME)) {
                    lore.append("lime");
                } else if (c.equals(Color.GREEN)) {
                    lore.append("green");
                } else if (c.equals(Color.AQUA)) {
                    lore.append("aqua");
                } else if (c.equals(Color.TEAL)) {
                    lore.append("teal");
                } else if (c.equals(Color.BLUE)) {
                    lore.append("blue");
                } else if (c.equals(Color.NAVY)) {
                    lore.append("navy");
                } else if (c.equals(Color.FUCHSIA)) {
                    lore.append("fuchsia");
                } else if (c.equals(Color.PURPLE)) {
                    lore.append("purple");
                } else if (c.equals(Color.ORANGE)) {
                    lore.append("orange");
                } else {
                    lore.append(c.asRGB());
                }
                lore.append("\n");
            }

            lore.append("Fade colors: \n");
            for (Color c : effect.getFadeColors()) {
                lore.append(" - ");
                if (c.equals(Color.WHITE)) {
                    lore.append("white");
                } else if (c.equals(Color.SILVER)) {
                    lore.append("silver");
                } else if (c.equals(Color.GRAY)) {
                    lore.append("gray");
                } else if (c.equals(Color.BLACK)) {
                    lore.append("black");
                } else if (c.equals(Color.RED)) {
                    lore.append("red");
                } else if (c.equals(Color.MAROON)) {
                    lore.append("maroon");
                } else if (c.equals(Color.YELLOW)) {
                    lore.append("yellow");
                } else if (c.equals(Color.OLIVE)) {
                    lore.append("olive");
                } else if (c.equals(Color.LIME)) {
                    lore.append("lime");
                } else if (c.equals(Color.GREEN)) {
                    lore.append("green");
                } else if (c.equals(Color.AQUA)) {
                    lore.append("aqua");
                } else if (c.equals(Color.TEAL)) {
                    lore.append("teal");
                } else if (c.equals(Color.BLUE)) {
                    lore.append("blue");
                } else if (c.equals(Color.NAVY)) {
                    lore.append("navy");
                } else if (c.equals(Color.FUCHSIA)) {
                    lore.append("fuchsia");
                } else if (c.equals(Color.PURPLE)) {
                    lore.append("purple");
                } else if (c.equals(Color.ORANGE)) {
                    lore.append("orange");
                } else {
                    lore.append(c.asRGB());
                }
                lore.append("\n");
            }

            lore.append("Flicker: ").append(effect.hasFlicker());
        }

        return new ItemBuilder(Material.FIREWORK_STAR).name("&eCurrent Effect").lore(lore.toString()).toItem();
    }

}
