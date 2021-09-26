package com.crazicrafter1.lootcrates.editor.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SingleCrateChangeItemMenu extends SimplexMenu {

    public SingleCrateChangeItemMenu(Crate crate) {
        super("change item ...", 5, BACKGROUND_1);

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

        // Original crate
        setComponent(1, 1, new Component() {
            @Override
            public ItemStack getIcon() {
                return crate.itemStack;
            }
        });

        // Change to
        RemovableComponent rem = new RemovableComponent(null);
        setComponent(4, 1, rem);

        // Confirm
        setComponent(7, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                Main.getInstance().info("Applying changes here!");
                ItemStack item = rem.getIcon();
                if (item != null) {
                    crate.itemStack = new ItemBuilder(item.getType()).mergeLexicals(item).toItem();
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.EMERALD_BLOCK).name("&6&lApply changes").toItem();
            }
        });

        // quartz
        // eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJmOTEwYzQ3ZGEwNDJlNGFhMjhhZjZjYzgxY2Y0OGFjNmNhZjM3ZGFiMzVmODhkYjk5M2FjY2I5ZGZlNTE2In19fQ==

        //setComponent(3, 1, new Component() {
        //    @Override
        //    public ItemStack getIcon() {
        //        return new ItemBuilder(Material.PLAYER_HEAD).skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJmM2EyZGZjZTBjM2RhYjdlZTEwZGIzODVlNTIyOWYxYTM5NTM0YThiYTI2NDYxNzhlMzdjNGZhOTNiIn19fQ==")
        //                .name("&8Change to").toItem();
        //    }
        //});

        // back
        backButton(4, 4, BACK_1, SingleCrateMenu.class, crate);
    }
}
