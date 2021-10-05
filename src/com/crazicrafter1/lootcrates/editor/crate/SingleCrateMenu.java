package com.crazicrafter1.lootcrates.editor.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Component;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.gapi.anvil.AnvilGUI;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.editor.loot.LootMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SingleCrateMenu extends SimplexMenu {



    public SingleCrateMenu(Crate crate) {
        super("Crate: " + crate.id, 5,
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
                new AnvilGUI.Builder()
                    .onClose(player -> new LootMenu().show(p))
                    .itemLeft(new ItemBuilder(Material.FEATHER).name("Use '&' for color codes").toItem())
                    .title("Edit title")
                    .onComplete((player, text) -> {
                        // invalid
                        //Main.get().info(text);
                        if (text.isEmpty()) {
                            return AnvilGUI.Response.text("Invalid.");
                        } else {
                            // add that lootgroup
                            crate.title = ChatColor.translateAlternateColorCodes('&', text);

                            return AnvilGUI.Response.close();
                        }
                    })
                    .plugin(Main.get())
                    .open(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.PAPER).name("&e&lChange Title").
                        lore("&8Current: &r" + crate.title).toItem();
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
                return new ItemBuilder(Material.SCAFFOLDING)
                        .name("&e&lChange Columns").count(col)
                        .lore("&8LMB: &c-\n&8RMB: &2+").toItem();
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
                        .lore("&8LMB: &c-\n&8RMB: &2+").toItem();


                //return new ItemBuilder(Material.END_CRYSTAL).name("&a&lChange Picks").count(picks).lore("&8Current: " + picks).toItem();
            }
        });

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<LootSet, Integer> entry : crate.lootByWeight.entrySet()) {
            LootSet lootGroup = entry.getKey();
            builder.append(String.format("&8 - %s  |  %s  |  %s\n",
                    entry.getKey().id, crate.getFormattedFraction(lootGroup), crate.getFormattedPercent(lootGroup)));
        }
        setComponent(2, 3, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // when clicking on this specific crate
                new SingleCrateLootMenu(crate, SingleCrateLootMenu.Mode.SELECT_EDIT).show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&a&lEdit Loot").lore(builder.toString()).toItem();
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
