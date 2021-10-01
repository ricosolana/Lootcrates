package com.crazicrafter1.lootcrates.editor.fireworks;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.editor.MainMenu;
import com.crazicrafter1.lootcrates.editor.crate.CrateMenu;
import com.crazicrafter1.lootcrates.editor.crate.SingleCrateMenu;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class FireworksMenu extends SimplexMenu {

    public FireworksMenu() {
        super("Firework", 5, BACKGROUND_1);
        Component inputPerimeter = new Component() {
            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.RED_STAINED_GLASS).name("&eSet to").toItem();
            }
        };

        setComponent(1 + 3, 0, inputPerimeter);
        setComponent(3, 1, inputPerimeter);
        setComponent(2 + 3, 1, inputPerimeter);
        setComponent(1 + 3, 2, inputPerimeter);

        // Original firework
        setComponent(1, 1, new Component() {
            @Override
            public ItemStack getIcon() {
                return new FireworkWrapper(Main.DAT.fireworkEffect).getStar();
            }
        });

        // Change to
        RemovableComponent rem = new RemovableComponent(null);
        setComponent(4, 1, rem);

        // Confirm
        setComponent(7, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {

                if (true)
                    return;

                Main.getInstance().info("Applying changes here!");
                ItemStack item = rem.getIcon();
                if (item != null) {
                    Main.getInstance().info(item.getType().name());
                    ItemMeta meta = item.getItemMeta();
                    if (meta instanceof FireworkEffectMeta fm) {
                        if (fm.hasEffect()) {
                            FireworkEffect effect = fm.getEffect();

                            Main.getInstance().config.set("firework.enabled", true);
                            Main.getInstance().config.set("firework.colors", effect.getColors());
                            Main.getInstance().config.set("firework.fade", effect.getFadeColors());
                            Main.getInstance().config.set("firework.flicker", effect.hasFlicker());
                        } else {
                            p.sendMessage(ChatColor.YELLOW + "");
                        }
                    } else {
                        p.sendMessage(ChatColor.YELLOW + "Not a firework");
                    }
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.EMERALD_BLOCK).name("&6&lApply changes").toItem();
            }
        });

        // back
        backButton(4, 4, BACK_1, MainMenu.class);


    }



}
