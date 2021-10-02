package com.crazicrafter1.lootcrates.crate;

import java.util.*;

import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.crutils.Bool;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public final class ActiveCrate {

    private static final class QSlot {
        boolean isSelected;
        AbstractLoot randomLoot;

        QSlot(boolean isSelected, AbstractLoot randomLoot) {
            this.isSelected = isSelected;
            this.randomLoot = randomLoot;
        }
    }

    // Constants
    private final Player player;
    private final int size;
    private final int picks;
    private final Sound sound;
    private final LootGroup[] lootChances;
    private final Inventory inventory;

    // Live variables
    private final HashMap<Integer, QSlot> slots = new HashMap<>();
    private State state = State.SELECTING;
    private int taskID = -1;
    private int lockSlot;

    ActiveCrate(Player p, Crate crate, int lockSlot) {
        this.player = p;
        this.size = crate.size;
        this.picks = crate.picks;
        this.sound = crate.sound;
        this.lootChances = new LootGroup[size];

        this.inventory = Bukkit.createInventory(p, size, crate.header);
        this.lockSlot = lockSlot;

        this.populate(crate);

        this.fill();
        p.openInventory(inventory);
    }

    private void populate(Crate crate) {
        for (int i = 0; i < size; i++) {
            LootGroup lootGroup = crate.getBasedRandom();
            this.lootChances[i] = lootGroup;
            Main.getInstance().info("lootgroup is null: " + (lootGroup == null));
        }
    }

    private void selectSlot(int slot) {

        if (slots.containsKey(slot))
            return;

        inventory.setItem(slot, Data.selectedItem);

        AbstractLoot randomLoot = lootChances[slot].getRandomLoot();
        slots.put(slot, new QSlot(true, randomLoot));


        if (sound != null)
            getPlayer().playSound(getPlayer().getLocation(), sound, 1, 1);

        // Play animatic on final pick
        if (slots.size() == picks) {

            // "Cost" of opening crate
            {
                ItemStack itemStack = getPlayer().getInventory().getItem(lockSlot);
                //noinspection ConstantConditions
                itemStack.setAmount(itemStack.getAmount() - 1);
            }

            // Free locked crate slot
            lockSlot = -1;

            state = State.REVEALING;

            if (Data.speed > 0) {

                 taskID = new BukkitRunnable() {
                     int iterations = 0;

                    @Override
                    public void run() {
                        // Panel reveal
                        if (iterations < size) {
                            inventory.setItem(iterations, getPanel(iterations));
                        }
                        else if (iterations > size + 10/Data.speed) {
                            this.cancel();
                            pop();
                        }

                        iterations++;
                    }
                }.runTaskTimer(Main.getInstance(), 20, Data.speed).getTaskId();

            } else {
                pop();
            }
        }
    }

    private void pop() {
        for (int i = 0; i < size; i++) {
            if (!slots.containsKey(i)) {
                inventory.setItem(i, null);
            } else inventory.setItem(i, getPanel(i));
        }

        if (Data.fireworkEffect != null) explosion();

        state = State.REVEALED;
    }

    private void explosion() {
        Location loc = getPlayer().getLocation();

        //noinspection ConstantConditions
        Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

        Main.crateFireworks.add(firework.getUniqueId());

        FireworkMeta fwm = firework.getFireworkMeta();
        fwm.addEffects(Data.fireworkEffect);
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
        // Give items if still making selecting
        if (state != State.SELECTING) {
            for (int slot : slots.keySet()) {
                slots.get(slot).randomLoot.execute(this,
                        true, null);
            }
            Data.totalOpens++;
        }

        if (taskID != -1)
            Main.getInstance().getServer().getScheduler().cancelTask(taskID);
    }

    private void fill() {
        for (int i = 0; i < size; i++) inventory.setItem(i, Data.unSelectedItem);
    }

    public Player getPlayer() {
        //return Bukkit.getPlayer(this.owner);
        return player;
    }

    private ItemStack getPanel(int slot) {
        return this.lootChances[slot].itemStack;
    }

    public void onInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!e.isShiftClick() && e.isLeftClick()) {
            int slot = e.getSlot();

            // If crate GUI clicked on
            if (e.getClickedInventory() == inventory) {
                switch (state) {
                    case SELECTING -> selectSlot(slot);
                    case REVEALING -> {}
                    case REVEALED -> {
                        // If slot is selected
                        QSlot qSlot = slots.get(slot);
                        if (qSlot == null
                                || flipSlot(slot, qSlot)
                                || getPlayer().getItemOnCursor().getType() != Material.AIR) {
                            return;
                        }

                        // Execute the loot action
                        Bool giveItem = new Bool(true); // Encapsulated boolean (bool ptr)
                        qSlot.randomLoot.execute(this, false, giveItem);

                        if (!giveItem.value) {
                            inventory.setItem(slot, null);
                        } else
                            e.setCancelled(false);

                        slots.remove(slot);
                    }
                }
            } else if (e.getClickedInventory() == e.getWhoClicked().getInventory()) {
                if (slot != lockSlot) {
                    e.setCancelled(false);
                }
            }
        }
    }
}