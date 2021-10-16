package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public final class ActiveCrate {

    private static final class QSlot {
        boolean isSelected;
        ILoot randomLoot;

        QSlot(boolean isSelected, ILoot randomLoot) {
            this.isSelected = isSelected;
            this.randomLoot = randomLoot;
        }
    }

    // Constants
    private final Player player;
    private final int size;
    private final int picks;
    private final Sound sound;
    private final LootSet[] lootChances;
    private final Inventory inventory;

    // Live variables
    private final HashMap<Integer, QSlot> slots = new HashMap<>();
    private State state = State.SELECTING;
    private int taskID = -1;
    private int lockSlot;

    private static final Data data = Main.get().data;

    public ActiveCrate(Player p, Crate crate, int lockSlot) {
        this.player = p;
        this.size = crate.columns * 9;
        this.picks = crate.picks;
        this.sound = crate.sound;
        this.lootChances = new LootSet[size];

        this.inventory = Bukkit.createInventory(p, size, crate.title);
        this.lockSlot = lockSlot;

        this.populate(crate);

        this.fill();
        p.openInventory(inventory);
    }

    private void populate(Crate crate) {
        for (int i = 0; i < size; i++) {
            LootSet lootGroup = crate.getBasedRandom();
            this.lootChances[i] = lootGroup;
        }
    }

    private void selectSlot(int slot) {

        if (slots.containsKey(slot))
            return;

        inventory.setItem(slot, data.selectedItem);

        ILoot randomLoot = lootChances[slot].getRandomLoot();
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

            if (data.speed > 0) {

                 taskID = new BukkitRunnable() {
                     int iterations = 0;

                    @Override
                    public void run() {
                        // Panel reveal
                        if (iterations < size) {
                            inventory.setItem(iterations, getPanel(iterations));
                        }
                        else if (iterations > size + 10/data.speed) {
                            this.cancel();
                            pop();
                        }

                        iterations++;
                    }
                }.runTaskTimer(Main.get(), 20, data.speed).getTaskId();

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

        if (data.fireworkEffect != null) explosion();

        state = State.REVEALED;
    }

    private void explosion() {
        Location loc = getPlayer().getLocation();

        //noinspection ConstantConditions
        Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

        Main.get().crateFireworks.add(firework.getUniqueId());

        FireworkMeta fwm = firework.getFireworkMeta();
        fwm.addEffects(data.fireworkEffect);
        firework.setFireworkMeta(fwm);

        firework.detonate();

        new BukkitRunnable() {
            @Override
            public void run() {
                Main.get().crateFireworks.remove(firework.getUniqueId());
            }
        }.runTaskLater(Main.get(), 1);
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

    public void close() {
        // Give items if still making selecting
        if (state != State.SELECTING) {
            for (Map.Entry<Integer, QSlot> entry : slots.entrySet()) {
                if (entry.getValue().randomLoot.execute(this)) {
                    Util.giveItemToPlayer(player, inventory.getItem(entry.getKey()));
                }
            }

            data.totalOpens++;
        }

        if (taskID != -1)
            Main.get().getServer().getScheduler().cancelTask(taskID);
    }

    private void fill() {
        for (int i = 0; i < size; i++)
            inventory.setItem(i, data.unSelectedItem);
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
                    case SELECTING: selectSlot(slot); break;
                    case REVEALING: break;
                    case REVEALED: {
                        // If slot is selected
                        QSlot qSlot = slots.get(slot);
                        if (qSlot == null
                                || flipSlot(slot, qSlot)
                                || getPlayer().getItemOnCursor().getType() != Material.AIR) {
                            return;
                        }

                        // Give item
                        if (qSlot.randomLoot.execute(this))
                            e.setCancelled(false);
                        else // Remove item
                            inventory.setItem(slot, null);

                        slots.remove(slot);
                        break;
                    }
                }
            } else if (e.getClickedInventory() == e.getWhoClicked().getInventory()) {
                if (slot != lockSlot) {
                    e.setCancelled(false);
                }
            }
        }
    }

    public enum State {
        SELECTING,
        REVEALING,
        REVEALED
    }
}