package com.crazicrafter1.lootcrates.editor.crate;

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
        super("Crate: " + crate.name, 5,
                new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("").toItem());

        setComponent(1, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // when clicking on this specific crate
                new SingleCrateChangeItemMenu(crate).show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(crate.itemStack.getType()).name("&b&lChange Item").resetLore().toItem();
            }
        });

        setComponent(3, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // when clicking on this specific crate
                // openanvilgui
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.PAPER).name("&e&lChange Title").
                        lore("&8Current: &r" + crate.header).toItem();
            }
        });

        setComponent(5, 1, new TriggerComponent() {

            final int col = crate.size / 9;

            @Override
            public void onLeftClick(Player p, boolean shift) {
                // decrement
                if (col != 1) {
                    crate.size -= 9;
                    if (crate.picks > crate.size)
                        crate.picks = crate.size;
                    new SingleCrateMenu(crate).show(p);
                }
            }

            @Override
            public void onRightClick(Player p, boolean shift) {
                // increment
                if (col != 6) {
                    crate.size += 9;
                    new SingleCrateMenu(crate).show(p);
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.SCAFFOLDING).
                        name("&e&lChange Columns").count(col).lore("\n&8LMB: -\n&8RMB: +").toItem();
            }
        });

        setComponent(7, 1,  new TriggerComponent() {

            final int picks = crate.picks;

            @Override
            public void onLeftClick(Player p, boolean shift) {
                // decrement
                if (picks > 1) {
                    crate.picks--;
                    new SingleCrateMenu(crate).show(p);
                }
            }

            @Override
            public void onRightClick(Player p, boolean shift) {
                // increment
                if (picks < crate.size) {
                    crate.picks++;
                    new SingleCrateMenu(crate).show(p);
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.END_CRYSTAL)
                        .name("&a&lChange Picks").count(picks)
                        .lore("\n&8LMB: -\n&8RMB: +").toItem();


                //return new ItemBuilder(Material.END_CRYSTAL).name("&a&lChange Picks").count(picks).lore("&8Current: " + picks).toItem();
            }
        });

        StringBuilder builder = new StringBuilder("&8Current: \n");
        for (Map.Entry<LootGroup, Integer> entry : crate.lootGroupsByWeight.entrySet()) {
            LootGroup lootGroup = entry.getKey();
            builder.append(String.format("&8 - %s  |  %s  |  %s\n",
                    entry.getKey().name, crate.getFormattedFraction(lootGroup), crate.getFormattedPercent(lootGroup)));
        }
        setComponent(2, 3, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // when clicking on this specific crate
                new SingleCrateLootMenu(crate, SingleCrateLootMenu.Mode.SELECT_EDIT).show(p);
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
