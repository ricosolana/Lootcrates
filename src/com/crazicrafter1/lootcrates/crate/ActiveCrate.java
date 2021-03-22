package com.crazicrafter1.lootcrates.crate;

import java.util.*;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.Util;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public final class ActiveCrate {


    private class QSlot {
        boolean isSelected;
        AbstractLoot randomLoot;

        QSlot(boolean isSelected, AbstractLoot randomLoot) {
            this.isSelected = isSelected;
            this.randomLoot = randomLoot;
        }
    }

    //private boolean kill = false;

    private final UUID owner;

    //private HashSet<Integer> slotsSelected;
    //private HashMap<Integer, AbstractLoot> slotLoot;
    private HashMap<Integer, QSlot> slots;

    private Inventory inventory;
    private final LootGroup[] lootChances;

    //private HashMap<Integer, ArrayList<String>> commandEventSlots = new HashMap<>();

    private boolean revealing = false;
    private boolean revealed = false;

    private final Crate crate;

    private int lockSlot;

    //private int iterations = 0;
    private final int SIZE;

    private int taskID = -1;

    ActiveCrate(Player p, Crate crate, int lockSlot) { //}, Main pl) {
        //this.slotsSelected = new HashSet<>();
        this.slots = new HashMap<>();
        this.SIZE = Main.inventorySize;
        this.inventory = Bukkit.createInventory(p, SIZE, Main.inventoryName);
        //this.lootChances = new String[s];
        this.lootChances = new LootGroup[SIZE];

        this.owner = p.getUniqueId();
        this.crate = crate;
        this.lockSlot = lockSlot;

        this.populateRandoms();

        this.fill();
        getPlayer().openInventory(inventory);
    }

    private void populateRandoms() {
        for (int i = 0; i < SIZE; i++) {
            LootGroup lootGroup = crate.getBasedRandom();
            Main.getInstance().debug(lootGroup.getName());
            this.lootChances[i] = lootGroup;
        }
    }

    //private HashMap<Integer, ArrayList<String>> getCommandEventSlots() {
    //    return commandEventSlots;
    //}

    private void selectSlot(int slot) {

        if (!revealing && !revealed && slot >= 0 && slot < SIZE) {
            //if (slotsSelected.contains(slot)) return;
            if (slots.containsKey(slot)) return;

            // 'select' the slot (changes the item)
            inventory.setItem(slot, Main.selectedItem);




            AbstractLoot randomLoot = lootChances[slot].getRandomLoot();
            slots.put(slot, new QSlot(true, randomLoot));
            //plugin.debug("Set to selected (SelectSlot) with loot " + item.getType().name());
            Main.getInstance().debug("abstractLoot: " + randomLoot.getClass().getSimpleName());


            if (Main.selectionSound != null)
                getPlayer().playSound(getPlayer().getLocation(), Main.selectionSound, 1, 1);

            // last slot was selected
            //if (slotsSelected.size() == config.getCrateSelections()) {
            if (slots.size() == Main.selections) {
                // Invalidate lockslot, so that it may be reused for whatever (since crate was consumed)
                lockSlot = -1;

                getPlayer().getInventory().getItemInMainHand().setAmount(getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                //isRevealing = true;

                revealing = true;

                if (Main.raffleSpeed > 0) {

                     taskID = new BukkitRunnable() {
                         int iterations = 0;

                        @Override
                        public void run() {
                            // Revealing each panel one by one
                            Main.getInstance().debug("iterations: " + iterations);
                            if (iterations < SIZE) {
                                inventory.setItem(iterations, getPanel(iterations));
                                Main.getInstance().debug("set loot tier (glass) " + iterations);
                            }

                            // POP!!!
                            else if (iterations > SIZE + 10/Main.raffleSpeed) {
                                this.cancel();
                                pop();
                            }

                            iterations++;
                        }
                    }.runTaskTimer(Main.getInstance(), 20, Main.raffleSpeed).getTaskId();

                } else {
                    pop();
                }
            }
        }
    }

    private void pop() {
        for (int i = 0; i < SIZE; i++) {
            if (!slots.containsKey(i)) {
                inventory.setItem(i, null);
            } else inventory.setItem(i, getPanel(i));
        }

        if (Main.enableFirework) doExplosion();

        revealed = true;
        revealing = false;
    }

    private void doExplosion() {
        Location loc = getPlayer().getLocation();

        Firework fireWork = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

        Main.crateFireWorks.add(fireWork.getUniqueId());

        FireworkMeta fwm = fireWork.getFireworkMeta();
        fwm.addEffects(Main.fireworkEffect);
        fireWork.setFireworkMeta(fwm);

        fireWork.detonate();

        new BukkitRunnable() {
            @Override
            public void run() {
                Main.crateFireWorks.remove(fireWork.getUniqueId());
            }
        }.runTaskLater(Main.getInstance(), 2);
    }

    /**
     * Assumes that crate loot already revealed, and clicking a "QSlot"
     */
    private boolean flipSlot(int slot, QSlot qSlot) {
        if (!qSlot.isSelected) return false;

        ItemStack visual = qSlot.randomLoot.getAccurateVisual(); //crate.lootGroups.g

        inventory.setItem(slot, visual);
        qSlot.isSelected = false;

        return true;
    }

    /*
        TODO
        test whether player is online,
        whether is dead,
        any sketch cases
        etc...
     */
    void close() {
        //for (int slot : slotsSelected) {
        if (revealing || revealed)
            for (int slot : slots.keySet()) {
                // give player the loot they didnt get
                // If is a pane still, give random item, else give the inventory item
                //if (slots.get(slot).isSelected) {



                if (slots.get(slot).isSelected || !(slots.get(slot).randomLoot instanceof LootItem)) {
                    slots.get(slot).randomLoot.perform(this);
                } else {
                    Util.giveItemToPlayer(getPlayer(), inventory.getItem(slot));
                }

                    //this.lootChances[slot].getRandomLoot().perform(this);
                //    return;
                //}
                //Util.giveItemToPlayer(getPlayer(), inventory.getItem(slot));
            }
        //kill = true;

        if (taskID != -1)
            Main.getInstance().getServer().getScheduler().cancelTask(taskID);
    }

    private void fill() {
        for (int i = 0; i < SIZE; i++) inventory.setItem(i, Main.unSelectedItem);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.owner);
    }

    private ItemStack getPanel(int slot) // pane from slot
    {
        return this.lootChances[slot].getPanel();
    }

    public void onInventoryClick(InventoryClickEvent e) {
        // Valid clicks
        if (!e.isShiftClick() && (e.isLeftClick() || e.isRightClick())) {
            int slot = e.getRawSlot();

            // If crate space was clicked on
            if (Util.inRange(slot, 0, SIZE - 1)) {

                // Try to select a slot
                if (!revealed && !revealing) {
                    selectSlot(slot);
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
                    player must have clicked a revealed, non paned item
                 */

                Main.getInstance().debug("Failed toggle pane --> item");

                // if cursor took an item
                Main.getInstance().debug("item in hand: " + getPlayer().getItemOnCursor().getType().name());

                // If item placed back into crate
                if (getPlayer().getItemOnCursor().getType() != Material.AIR) {
                    e.setCancelled(true);
                    return;
                }


                // If player explicitly removed an item (CLICKED)
                // Let the player have freely have the item if it is a LootItem
                // test instance of AbstractLoot
                slots.remove(slot);



                if (qSlot.randomLoot instanceof LootItem) {

                    // do not cancel event, give them the item
                    Main.getInstance().debug("Letting have loot! " + qSlot.randomLoot.getClass().getName());
                    return;
                }

                Main.getInstance().debug("Performing action!");

                // else, an event or command slot / macro etc ... was clicked
                // do the thing, and cancel event
                qSlot.randomLoot.perform(this);

                inventory.setItem(slot, null);

                e.setCancelled(true);

            } else if (slot >= SIZE && slot < SIZE + 36) {
                int offset = 27 + SIZE;

                if (slot - offset == lockSlot) {
                    e.setCancelled(true);
                }
            }
        } else {
            e.setCancelled(true);
        }
    }

}