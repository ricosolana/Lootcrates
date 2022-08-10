package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.RewardSettings;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CrateInstance {

    private static final class QSlot {
        boolean isHidden = true;
        ILoot randomLoot;

        QSlot(ILoot randomLoot) {
            this.randomLoot = randomLoot;
        }
    }

    // Constants
    private final Player player;
    private final CrateSettings crate;
    private final int size;
    private final int picks;
    private final Sound sound;
    private final LootSetSettings[] lootChances;
    private final Inventory inventory;

    // Live variables
    private final HashMap<Integer, QSlot> slots = new HashMap<>();
    private State state = State.SELECTING;
    private int taskID = -1;
    private int lockSlot;

    private final RewardSettings data = Main.get().rewardSettings;

    public CrateInstance(Player p, CrateSettings crate, int lockSlot) {
        this.player = p;
        this.crate = crate;
        this.size = crate.columns * 9;
        this.picks = crate.picks;
        this.sound = crate.sound;
        this.lootChances = new LootSetSettings[size];

        this.inventory = Bukkit.createInventory(p, size, ColorUtil.renderAll(crate.getTitle(p)));
        this.lockSlot = lockSlot;

        this.populate(crate);

        this.fill();
        //p.openInventory(inventory);
    }

    public void open() {
        Main.get().openCrates.put(player.getUniqueId(), this);
        player.openInventory(inventory);
    }

    private void populate(CrateSettings crate) {
        for (int i = 0; i < size; i++) {
            this.lootChances[i] = crate.loot.getRandom();
        }
    }

    private void selectSlot(int slot) {
        if (slots.containsKey(slot))
            return;

        inventory.setItem(slot, data.selectedItemStack(player, crate));

        ILoot randomLoot = lootChances[slot].getRandomLoot();
        slots.put(slot, new QSlot(randomLoot));

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

            // Free the locked crate slot
            lockSlot = -1;

            state = State.REVEALING;

            if (data.speed != 0) {
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

        // fixme this might be causing the spam click bug
        // putting into a runnable might fix
        //new BukkitRunnable() {
        //    @Override
        //    public void run() {
                state = State.REVEALED;
        //    }
        //}.runTask(Main.get());
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
        if (!qSlot.isHidden) {
            return false;
        }

        ItemStack visual = qSlot.randomLoot.getRenderIcon(player);

        inventory.setItem(slot, visual);
        qSlot.isHidden = false;

        return true;
    }

    public void close() {
        // Give items if still making selecting
        if (state != State.SELECTING) {
            for (Map.Entry<Integer, QSlot> entry : slots.entrySet()) {
                if (entry.getValue().randomLoot.execute(this)) {
                    if (entry.getValue().isHidden)
                        Util.give(player, entry.getValue().randomLoot.getRenderIcon(player));
                    else
                        Util.give(player, inventory.getItem(entry.getKey()));
                }
            }
        }

        Main.get().getStat(player.getUniqueId()).crateInc(this.crate.id);

        if (state == State.REVEALING)
            Main.get().getServer().getScheduler().cancelTask(taskID);
    }

    private void fill() {
        for (int i = 0; i < size; i++)
            inventory.setItem(i, data.unSelectedItemStack(player, crate));
    }

    public Player getPlayer() {
        return player;
    }

    private ItemStack getPanel(int slot) {
        return this.lootChances[slot].itemStack(player);
    }

    public void onInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (e.getClick() != ClickType.DOUBLE_CLICK
                && !e.isShiftClick()
                && e.isLeftClick()) {

            int slot = e.getSlot();

            // If crate GUI clicked on
            // On Mohist and certain versions, '==' does not work, meaning that the same representing
            // object does not share the same address space (so not same object but still could be same inventory)
            if (Objects.equals(e.getClickedInventory(), inventory)) {

                switch (state) {
                    case SELECTING:
                        selectSlot(slot);
                        break;
                    case REVEALING:
                        //do nothing
                        break;
                    case REVEALED: {

                        // If slot is selected
                        QSlot qSlot = slots.get(slot);

                        // If slot does not exist
                        // If there was an item on mouse virtual slot
                        if (qSlot == null
                                || getPlayer().getItemOnCursor().getType() != Material.AIR)
                            return;

                        // If the slot was flipped, do nothing else
                        if (flipSlot(slot, qSlot))
                            return;

                        // Give item
                        if (qSlot.randomLoot.execute(this)) {
                            e.setCancelled(false);
                        } else // Remove item
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