package com.crazicrafter1.lootcrates.editor.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Component;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SingleCrateMenu extends SimplexMenu {



    public SingleCrateMenu(Crate crate) {
        super("Crate: " + crate.name, 5,
                new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("").toItem());

<<<<<<< HEAD
=======
        final FileConfiguration config = Main.getInstance().config;
        final String path = "crates." + crate.name + ".";

>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
        setComponent(1, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                // when clicking on this specific crate
                new SingleCrateChangeItemMenu(crate).show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(crate.itemStack.getType()).name("&b&lChange Item").resetLore().toItem();
            }
        });

        /*
        setComponent(3, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                // when clicking on this specific crate
                // openanvilgui
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.PAPER).name("&e&lChange Title").
                        lore("&8Current: &r" + crate.header).toItem();
            }
        });
         */

        setComponent(5, 1, new TriggerComponent() {

<<<<<<< HEAD
            final int col = crate.size / 9;
=======
            int col = crate.size / 9;
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109

            @Override
            public void onLeftClick(Player p) {
                // decrement
                if (col != 1) {
<<<<<<< HEAD
                    crate.size -= 9;
                    if (crate.picks > crate.size)
                        crate.picks = crate.size;
                    new SingleCrateMenu(crate).show(p);
=======
                    config.set(path + "columns", --col);
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
                }
            }

            @Override
<<<<<<< HEAD
            public void onRightClick(Player p) {
                // increment
                if (col != 6) {
                    crate.size += 9;
                    new SingleCrateMenu(crate).show(p);
=======
            public void onMiddleClick(Player p) {
                // default
                config.set(path + "columns", null);
            }

            @Override
            public void onRightClick(Player p) {
                // increment
                if (col != 6) {
                    config.set(path + "columns", ++col);
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.SCAFFOLDING).
                        name("&e&lChange Columns").count(col).lore(" - left click to decrement\n - right click to increment").toItem();
            }
        });

        setComponent(7, 1,  new TriggerComponent() {

<<<<<<< HEAD
            final int picks = crate.picks;
=======
            int picks = crate.picks;
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109

            @Override
            public void onLeftClick(Player p) {
                // decrement
<<<<<<< HEAD
                if (picks > 1) {
                    crate.picks--;
=======
                if (picks != 1) {
                    config.set(path + "picks", --picks);
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
                    new SingleCrateMenu(crate).show(p);
                }
            }

            @Override
<<<<<<< HEAD
            public void onRightClick(Player p) {
                // increment
                if (picks < crate.size) {
                    crate.picks++;
=======
            public void onMiddleClick(Player p) {
                // set to default reliance
                config.set(path + "picks", null);
                new SingleCrateMenu(crate).show(p);
            }

            @Override
            public void onRightClick(Player p) {
                // increment
                if (picks != crate.size/9) {
                    config.set(path + "picks", ++picks);
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
                    new SingleCrateMenu(crate).show(p);
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.END_CRYSTAL).
                        name("&a&lChange Picks").count(picks).lore(" - left click to decrement\n - right click to increment").toItem();


                //return new ItemBuilder(Material.END_CRYSTAL).name("&a&lChange Picks").count(picks).lore("&8Current: " + picks).toItem();
            }
        });

        StringBuilder builder = new StringBuilder("&8Current: \n");
<<<<<<< HEAD
        int prevSum = 0;
        for (Map.Entry<String, Integer> entry : crate.lootGroups.entrySet()) {

            int weight = entry.getValue() - prevSum;
            float percent = 100.f * ((float) weight / (float) crate.totalWeights);

            builder.append(String.format("&8 - %s  |  %d/%d  |  %.02f%%\n", entry.getKey(), weight, crate.totalWeights, percent));

            prevSum = entry.getValue();
=======
        for (Map.Entry<String, Integer> entry : crate.lootGroups.entrySet()) {
            builder.append(" - ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
        }
        setComponent(2, 3, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                // when clicking on this specific crate
                new SingleCrateLootMenu(crate).show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&a&lChange Loot").lore(builder.toString()).toItem();
            }
        });

        setComponent(6, 3, new Component() {
            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.ANVIL).name("&7&lWeights").lore("&8Total weights: " + crate.totalWeights).toItem();
            }
        });

        backButton(4, 4, BACK_1, CrateMenu.class);
    }
}
