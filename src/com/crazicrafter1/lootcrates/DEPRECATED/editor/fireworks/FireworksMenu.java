package com.crazicrafter1.lootcrates.editor.fireworks;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Component;
import com.crazicrafter1.gapi.RemovableComponent;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.editor.MainMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
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
                return new ItemBuilder(Material.FIREWORK_STAR)
                        .fireworkEffect(Main.get().data.fireworkEffect).toItem();
            }
        });

        // Change to
        RemovableComponent rem = new RemovableComponent(null);
        setComponent(4, 1, rem);

        // Confirm
        setComponent(7, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                ItemStack item = rem.getIcon();
                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta instanceof FireworkEffectMeta fm) {
                        if (fm.hasEffect()) {
                            Main.get().data.fireworkEffect = fm.getEffect();
                            new FireworksMenu().show(p);
                        } else {
                            p.sendMessage(ChatColor.YELLOW + "must have effects");
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
