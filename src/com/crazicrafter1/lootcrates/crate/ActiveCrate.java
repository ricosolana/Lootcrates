package com.crazicrafter1.lootcrates.crate;

import java.util.*;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.Util;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public final class ActiveCrate {


    private static class QSlot {
        public boolean isSelected;
        public AbstractLoot randomLoot;

        public QSlot(boolean isSelected, AbstractLoot randomLoot) {
            this.isSelected = isSelected;
            this.randomLoot = randomLoot;
        }
    }

    private boolean kill = false;

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

    private final int lockSlot;

    private int iterations = 0;
    private final int SIZE;

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
            this.lootChances[i] = crate.getBasedRandom();
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



            // do not need to put this here, can put later
            //ItemStack item = getRewardItem(lootChances[slot], slot);

            //slotsSelected.add(slot);
            slots.put(slot, new QSlot(true, null));
            //plugin.debug("Set to selected (SelectSlot) with loot " + item.getType().name());



            if (Main.selectionSound != null)
                getPlayer().playSound(getPlayer().getLocation(), Main.selectionSound, 1, 1);

            // last slot was selected
            //if (slotsSelected.size() == config.getCrateSelections()) {
            if (slots.size() == Main.selections) {
                getPlayer().getInventory().getItemInMainHand().setAmount(getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                //isRevealing = true;

                revealing = true;

                if (Main.raffleSpeed > 0) {

                     new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!kill) {
                                /*
                                    scrolling
                                 */
                                Main.getInstance().debug("iterations: " + iterations);
                                if (iterations < SIZE) {
                                    inventory.setItem(iterations, getChancePane(iterations));
                                    //getPlayer().closeInventory();//inventory.
                                    //getPlayer().o
                                    Main.getInstance().debug("set loot tier (glass) " + iterations);
                                }

                                /*
                                    reveal selected panes
                                 */
                                else {
                                    if (iterations > SIZE + 10/Main.raffleSpeed) {

                                        /*
                                         * Remove all other options except selected slots
                                         */
                                        for (int i = 0; i < SIZE; i++) {
                                            //if (!slotsSelected.contains(i)) {
                                            if (!slots.containsKey(i)) {
                                                inventory.setItem(i, null);
                                            }
                                        }

                                        //                                Main.getInstance().getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "");

                                        if (Main.enableFirework) launchFirework();

                                        revealed = true;
                                        revealing = false;

                                        iterations = 0;

                                        this.cancel();
                                        return;

                                        //this.cancel();
                                    }
                                }

                                iterations++;
                            }
                            else {
                                this.cancel(); //Bukkit.getScheduler().cancelTask(taskID); //else this.cancel();
                                return;
                            }

                        }
                    }.runTaskTimer(Main.getInstance(), 20, Main.raffleSpeed);

                } else {
                    //Sound.valueOf()
                    for (int i = 0; i < SIZE; i++) {
                        inventory.setItem(i, getChancePane(i));

                        //if (!slotsSelected.contains(i)) {
                        if (!slots.containsKey(i)) {
                            inventory.setItem(i, null);
                        }
                    }

                    //                                Main.getInstance().getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "");

                    if (Main.enableFirework) launchFirework();

                    revealed = true;
                    revealing = false;
                }
            }
        }
    }

    private void launchFirework() {

        // summon fireworks
        Location loc = getPlayer().getLocation();

        Firework fireWork = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

        /*
         * TODO
         * Remove Firework entity, use UUID instead
         */

        Main.crateFireWorks.add(fireWork);

        FireworkMeta fwm = fireWork.getFireworkMeta();

        fwm.addEffects(Main.fireworkEffect);

        fireWork.setFireworkMeta(fwm);

        fireWork.detonate();

        new BukkitRunnable() {
            @Override
            public void run() {
                Main.crateFireWorks.remove(fireWork);
            }
        }.runTaskLater(Main.getInstance(), 2);

    }

    /**
     * Assumes that crate loot already revealed, and clicking a "QSlot"
     */
    private boolean flipSlot(int slot, QSlot qSlot) {
        //if (!slotsSelected.contains(slot)) return false;
        //if (!slots.containsKey(slot)) return false;

        if (!qSlot.isSelected) return false;

        ItemStack visual = lootChances[slot].getRandomLoot().getAccurateVisual(); //crate.lootGroups.g

        inventory.setItem(slot, visual);
        //slotsSelected.remove(slot);
        //slots..remove(slot);
        qSlot.isSelected = false;

        return true;
    }

    /*
        TODO
        test whether player is online,
        whether is dead,
        etc...
     */
    void close() {
        //for (int slot : slotsSelected) {
        for (int slot : slots.keySet()) {
            // give player the loot they didnt get
            // If is a pane still, give random item, else give the inventory item
            if (slots.get(slot).isSelected) {
                this.lootChances[slot].getRandomLoot().perform(this);
                return;
            }

            kill = true;

            Util.giveItemToPlayer(getPlayer(), inventory.getItem(slot));
            Main.openCrates.remove(owner);
        }
    }

    private void fill() {
        for (int i = 0; i < SIZE; i++) inventory.setItem(i, Main.unSelectedItem);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.owner);
    }

    private ItemStack getChancePane(int slot) // pane from slot
    {
        return this.lootChances[slot].getPanel();
    }

    public void onInventoryClick(InventoryClickEvent e) {

        /*
            TODO:
            test whether number keys can bypass this, and other keys maybe
         */

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
                // (if a pane was clicked)

                // Try to toggle a pane to an item
                //if (!slots.containsKey(slot))
                QSlot qSlot = slots.getOrDefault(slot, null);
                if (qSlot == null) {
                    e.setCancelled(true);
                    return;
                }

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

                // player pressed the item, do something


                // If player explicitly removed an item (CLICKED)
                // Let the player have freely have the item if it is a LootItem
                // test instance of AbstractLoot
                if (qSlot.randomLoot instanceof LootItem) {
                    // do not cancel event, give them the item
                    return;
                }

                // else, an event or command slot / macro etc ... was clicked
                // do the thing, and cancel event
                qSlot.randomLoot.perform(this);
                slots.remove(slot);
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

    @Deprecated
    public void onInventoryClose(InventoryCloseEvent e) {
        if (revealed || revealing) {
            //this.giveLoot();
            this.close();
            //giveRemaining();
            //runRemaining();
        }
        //destroy();
        Main.openCrates.remove(getPlayer().getUniqueId());
    }

    public void onInventoryDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Deprecated
    public void onPlayerQuit(PlayerQuitEvent e) {
        /*
            TODO
            test whether this works
         */
        close();
    }
}