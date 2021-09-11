package com.crazicrafter1.lootcrates.editor;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Component;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SingleCrateMenu extends SimplexMenu {

    public SingleCrateMenu(Crate crate) {
        super("Crate: " + crate.getName(), 3,
                new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("").toItem());

        ItemStack c = crate.getItemStack(1);

        setComponent(1, 0,
                new TriggerComponent(new ItemBuilder(c.getType()).name("&b&lChange Item").resetLore().toItem()) {
            @Override
            public void onLeftClick(Player p) {
                // when clicking on this specific crate
            }
        });

        setComponent(3, 0,
                new TriggerComponent(new ItemBuilder(Material.PAPER).name("&e&lChange Title").lore("&8Current: &r" + crate.getHeader()).toItem()) {
                    @Override
                    public void onLeftClick(Player p) {
                        // when clicking on this specific crate

                    }
                });

        int col = crate.getSize() / 9;
        setComponent(5, 0,
                new TriggerComponent(new ItemBuilder(Material.SCAFFOLDING).name("&e&lChange Columns").count(col).lore("&8Current: " + col).toItem()) {
                    @Override
                    public void onLeftClick(Player p) {
                        // when clicking on this specific crate

                    }
                });

        int picks = crate.getPicks();
        setComponent(7, 0,
                new TriggerComponent(new ItemBuilder(Material.END_CRYSTAL).name("&a&lChange Picks").count(picks).lore("&8Current: " + picks).toItem()) {
                    @Override
                    public void onLeftClick(Player p) {
                        // when clicking on this specific crate

                    }
                });

        StringBuilder builder = new StringBuilder("&8Current: \n");
        for (Map.Entry<LootGroup, Integer> entry : crate.getOriginalWeights().entrySet()) {
            builder.append(" - ").append(entry.getKey().name()).append(" : ").append(entry.getValue()).append("\n");
        }
        setComponent(2, 2,
                new TriggerComponent(new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&a&lChange Loot").lore(builder.toString()).toItem()) {
                    @Override
                    public void onLeftClick(Player p) {
                        // when clicking on this specific crate

                    }
                });

        setComponent(6, 2,
                new Component(new ItemBuilder(Material.ANVIL).name("&7&lWeights").lore("&8Total weights: " + crate.getTotalWeights()).toItem()));

    }
}
