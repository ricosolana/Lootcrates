package com.crazicrafter1.lootcrates.crate;

import java.util.*;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.util.Bool;
import com.crazicrafter1.lootcrates.util.ReflectionUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public final class ActiveCrate {

    private final class QSlot {
        boolean isSelected;
        AbstractLoot randomLoot;

        QSlot(boolean isSelected, AbstractLoot randomLoot) {
            this.isSelected = isSelected;
            this.randomLoot = randomLoot;
        }
    }

    // Constants
    private final UUID owner;
    private final int size;
    private final int picks;
    private int lockSlot;
    private final LootGroup[] lootChances;

    // Live variables
    private Inventory inventory;
    private HashMap<Integer, QSlot> slots = new HashMap<>();
    private boolean revealing = false;
    private boolean revealed = false;
    private int taskID = -1;

    ActiveCrate(Player p, Crate crate, int lockSlot) {
        this.owner = p.getUniqueId();
        this.size = crate.getSize();
        this.picks = crate.getPicks();
        this.lockSlot = lockSlot;
        this.lootChances = new LootGroup[size];

        this.inventory = Bukkit.createInventory(p, size, crate.getHeader());
        this.populate(crate);

        this.fill();
        p.openInventory(inventory);
    }

    private void populate(Crate crate) {
        for (int i = 0; i < size; i++) {
            LootGroup lootGroup = crate.getBasedRandom();
            Main.getInstance().debug(lootGroup.name());
            this.lootChances[i] = lootGroup;
        }
    }

    private boolean selectSlot(int slot) {

        if (!revealing && !revealed &&
                !slots.containsKey(slot)) {
            inventory.setItem(slot, Main.selectedItem);

            AbstractLoot randomLoot = lootChances[slot].getRandomLoot();
            slots.put(slot, new QSlot(true, randomLoot));
            Main.getInstance().debug("abstractLoot: " + randomLoot.getClass().getSimpleName());


            if (Main.sound != null)
                getPlayer().playSound(getPlayer().getLocation(), Main.sound, 1, 1);

            // Player finished selecting
            if (slots.size() == picks) {
                // Decrement held crate
                {
                    ItemStack itemStack = getPlayer().getInventory().getItem(lockSlot);
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }

                // Invalidate lockslot, so that it may be reused for whatever (since crate was consumed)
                lockSlot = -1;

                revealing = true;

                if (Main.speed > 0) {

                     taskID = new BukkitRunnable() {
                         int iterations = 0;

                        @Override
                        public void run() {
                            // Revealing each panel one by one
                            Main.getInstance().debug("iterations: " + iterations);
                            if (iterations < size) {
                                inventory.setItem(iterations, getPanel(iterations));
                                Main.getInstance().debug("set loot tier (glass) " + iterations);
                            }

                            // POP!!!
                            else if (iterations > size + 10/Main.speed) {
                                this.cancel();
                                pop();
                            }

                            iterations++;
                        }
                    }.runTaskTimer(Main.getInstance(), 20, Main.speed).getTaskId();

                } else {
                    pop();
                }
            }
            return true;
        }
        return false;
    }

    private void pop() {
        for (int i = 0; i < size; i++) {
            if (!slots.containsKey(i)) {
                inventory.setItem(i, null);
            } else inventory.setItem(i, getPanel(i));
        }

        if (Main.enableFirework) explosion();

        revealed = true;
        revealing = false;
    }

    private void explosion() {
        Location loc = getPlayer().getLocation();

        Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

        Main.crateFireworks.add(firework.getUniqueId());

        FireworkMeta fwm = firework.getFireworkMeta();
        fwm.addEffects(Main.fireworkEffect);
        firework.setFireworkMeta(fwm);

        firework.detonate();

        new BukkitRunnable() {
            @Override
            public void run() {
                Main.crateFireworks.remove(firework.getUniqueId());
            }
        }.runTaskLater(Main.getInstance(), 1);
    }

    /**
     * A panel was clicked, show it
     */
    private boolean flipSlot(int slot, QSlot qSlot) {
        if (!qSlot.isSelected) return false;

        ItemStack visual = qSlot.randomLoot.getIcon();

        inventory.setItem(slot, visual);
        qSlot.isSelected = false;

        return true;
    }

    void close() {
        /*
         * If point of no return is reached, give items
         */
        if (revealing || revealed) {
            for (int slot : slots.keySet()) {
                slots.get(slot).randomLoot.execute(this,
                        true, null);
            }
        }

        if (taskID != -1)
            Main.getInstance().getServer().getScheduler().cancelTask(taskID);
    }

    private void fill() {
        for (int i = 0; i < size; i++) inventory.setItem(i, Main.unSelectedItem);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.owner);
    }

    private ItemStack getPanel(int slot) {
        return this.lootChances[slot].itemStack();
    }

    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.isShiftClick() && e.isLeftClick()) {
            int slot = e.getSlot();

            // If crate GUI clicked on
            if (e.getClickedInventory() == inventory) {

                // Try to select a slot
                if (selectSlot(slot)) {
                    e.setCancelled(true);
                    return;
                }

                // If revealing, cancel click
                if (revealing) {
                    e.setCancelled(true);
                    return;
                }

                /*
                    At this point, crate has only the hidden 4 final loots to give
                 */

                // If a loot was revealed successfully,
                QSlot qSlot = slots.getOrDefault(slot, null);
                if (qSlot == null) {
                    e.setCancelled(true);
                    return;
                }

                // Try to toggle a panel to item
                if (flipSlot(slot, qSlot)) {
                    Main.getInstance().debug("Toggled pane --> item");
                    e.setCancelled(true);
                    return;
                }

                /*
                    Final case:
                    player must have clicked a revealed item
                 */

                Main.getInstance().debug("Failed toggle pane --> item");

                // if cursor took an item
                Main.getInstance().debug("item in hand: " + getPlayer().getItemOnCursor().getType().name());

                // If item placed back into crate
                if (getPlayer().getItemOnCursor().getType() != Material.AIR) {
                    e.setCancelled(true);
                    return;
                }

                // Execute the loot action
                Bool giveItem = new Bool(true);
                qSlot.randomLoot.execute(this, false, giveItem);

                if (!giveItem.value) {
                    //Main.getInstance().info("Deleting item");
                    inventory.setItem(slot, null);
                    e.setCancelled(true);
                }

                slots.remove(slot);

            } else if (e.getClickedInventory() == e.getWhoClicked().getInventory()) {
                if (slot == lockSlot) {
                    e.setCancelled(true);
                }
            }
        } else {
            e.setCancelled(true);
        }
    }

}