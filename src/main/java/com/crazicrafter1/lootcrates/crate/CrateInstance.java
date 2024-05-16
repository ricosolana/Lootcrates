package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.Lootcrates;
import com.crazicrafter1.lootcrates.PlayerLog;
import com.crazicrafter1.lootcrates.RewardSettings;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CrateInstance {

    /*
     * Runtime modifiable stuff
     */
    public static Map<UUID, CrateInstance> CRATES = new HashMap<>();
    public static Set<Firework> crateFireworks = Collections.newSetFromMap(new WeakHashMap<>());

    private static final class TmpSlot {
        ILoot randomLoot;
        boolean isHidden = true;

        TmpSlot(ILoot randomLoot) {
            this.randomLoot = randomLoot;
        }
    }

    // Constants
    private final Player player;
    private final CrateSettings crate;
    private final int size;
    private final int picks;
    private final Sound sound;
    private final LootCollection[] lootChances;
    private final Inventory inventory;

    // Live variables
    private final HashMap<Integer, TmpSlot> slots = new HashMap<>();
    private State state = State.SELECTING;
    private int taskID = -1;
    //private int lockSlot;
    private final ItemStack hostItem;

    private final RewardSettings data = LCMain.get().rewardSettings;

    public CrateInstance(Player p, CrateSettings crate, @Nullable ItemStack hostItem) {
        this.player = p;
        this.crate = crate;
        this.size = crate.columns * 9;
        this.picks = crate.picks;
        this.sound = crate.sound;
        this.lootChances = new LootCollection[size];

        this.inventory = Bukkit.createInventory(p, size, ColorUtil.renderAll(crate.getTitle(p)));
        this.hostItem = hostItem;

        this.populate(crate);

        this.fill();
    }

    public void open() {
        CRATES.put(player.getUniqueId(), this);
        player.openInventory(inventory);
    }

    private void populate(CrateSettings crate) {
        for (int i = 0; i < lootChances.length; i++) {
            this.lootChances[i] = crate.getRandomLootSet();
        }
    }

    private void selectSlot(int slot) {
        if (slots.containsKey(slot))
            return;

        inventory.setItem(slot, data.selectedItemStack(player, crate));

        ILoot randomLoot = lootChances[slot].getRandomLoot();
        slots.put(slot, new TmpSlot(randomLoot));

        if (sound != null)
            getPlayer().playSound(getPlayer().getLocation(), sound, 1, 1);

        // Play animatic on final pick
        if (slots.size() == picks) {
            // "Cost" of opening crate
            if (hostItem != null) {
                Validate.isTrue(!LCMain.get().checkCerts || Lootcrates.claimTicket(hostItem) != null);
                hostItem.setAmount(hostItem.getAmount() - 1);
            }

            state = State.REVEALING;

            if (data.speed != 0) {
                switch (crate.revealType) {
                    case GOOD_OL_DESTY:
                        startDestyAnimation();
                        break;
                    case WASD:
                        startWASDAnimation();
                        break;
                    case POPCORN:
                        startPopcornAnimation();
                        break;
                    default:
                        pop();
                        break;
                }
            } else {
                pop();
            }
        }
    }

    private void startDestyAnimation() {
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
        }.runTaskTimer(LCMain.get(), 20, data.speed).getTaskId();
    }

    // This is not the actual csgo animation because more than 1 pick can be picked
    // instead an animation of vertically/horizontally translating tiles in each row/col
    // will be played
    private void startWASDAnimation() {
        taskID = new BukkitRunnable() {
            final int maxIterations = size + 10 / data.speed;
            int iterations = 0;

            int x0, y0;

            @Override
            public void run() {

                // TODO
                //  maybe have rows with more selected icons to scroll faster than those with less

                // sequenced horizontal and vertical scrolling
                if (iterations < maxIterations / 4) {
                    x0++;
                } else if (iterations < maxIterations / 2) {
                    y0++;
                } else if (iterations >= maxIterations - maxIterations / 4) {
                    y0--;
                } else {
                    x0--;
                }

                //x0 %= 9;
                //y0 %= size / 9;

                // Set the panels from the left to right and shift each time
                for (int x = 0; x < 9; x++) {
                    for (int y = 0; y < size / 9; y++) {
                        // horizontal scrolling
                        //inventory.setItem(x + y * 9,
                        //        getPanel(((x + iterations) % 9) + y + 9)
                        //);

                        int ix = (x0 + x) % 9;
                        int iy = (y0 + y) % (size / 9);

                        if (ix < 0)
                            ix += 9;
                        if (iy < 0)
                            iy += size / 9;

                        //ItemBuilder.mut().gl

                        inventory.setItem(x + y * 9,
                                getPanel(ix + iy * 9)
                        );
                    }
                }

                // timer expiry check
                if (iterations > maxIterations) {
                    this.cancel();
                    pop();
                }

                iterations++;
            }
        }.runTaskTimer(LCMain.get(), 20, data.speed).getTaskId();
    }

    private void startPopcornAnimation() {
        taskID = new BukkitRunnable() {
            final int maxIterations = size + 10 / data.speed;
            int iterations = 0;

            //int delay = 10;
            double delay = 5;

            //int timer = 0;

            private final List<Integer> availableSlots;

            {
                availableSlots = IntStream.range(0, size).boxed().filter(i -> !slots.containsKey(i)).collect(Collectors.toList());
                Collections.shuffle(availableSlots);
            }

            @Override
            public void run() {
                double r = Math.random();
                double t = 1.0 / (1 + delay);
                if (delay <= 0 || r < t) {
                    delay -= r;

                    player.spawnParticle(r > t/3.0 ? Particle.SMOKE_LARGE : Particle.SMOKE_NORMAL,
                            player.getLocation(), (int)(20.0/(5.0 + delay)));

                    if (iterations < availableSlots.size()) {
                        inventory.setItem(availableSlots.get(iterations),
                                r < t/3.0 ? new ItemStack(Material.AIR) : ItemBuilder.copy(r > (t*2./3.) ? Material.POLISHED_BLACKSTONE_BUTTON : Material.DARK_OAK_BUTTON).name("&8Burnt").build());

                        if (r > t/3.0)
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1, (float)(.9 + r/20.0));
                        else
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, (float)(.8 + r/20.0));
                    }
                    else if (iterations > maxIterations) {
                        this.cancel();
                        pop();
                    }
                    iterations++;
                }
            }
        }.runTaskTimer(LCMain.get(), 20, 1).getTaskId();
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

        // TODO because the firework is purely visual
        //  make client side only
        // how? maybe protocol lib
        //  im not sure whether fireworks are server side or what

        //noinspection ConstantConditions
        Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

        // adding
        crateFireworks.add(firework);

        FireworkMeta fwm = firework.getFireworkMeta();
        fwm.addEffects(data.fireworkEffect);
        firework.setFireworkMeta(fwm);

        firework.detonate();
    }

    /**
     * A panel was clicked, show it
     */
    private boolean flipSlot(int slot, TmpSlot tmpSlot) {
        if (!tmpSlot.isHidden) {
            return false;
        }

        ItemStack visual = tmpSlot.randomLoot.getRenderIcon(player);

        inventory.setItem(slot, visual);
        tmpSlot.isHidden = false;

        return true;
    }

    public void close() {
        // Give items if still making selecting
        if (state == State.CLOSED) {
            return;
        }

        if (state != State.SELECTING) {
            for (Map.Entry<Integer, TmpSlot> entry : slots.entrySet()) {
                if (entry.getValue().randomLoot.execute(this)) {
                    if (entry.getValue().isHidden)
                        Util.give(player, entry.getValue().randomLoot.getRenderIcon(player));
                    else
                        Util.give(player, inventory.getItem(entry.getKey()));
                }
            }
        }

        PlayerLog.get(player.getUniqueId()).increment(this.crate.id);

        if (state == State.REVEALING) {
            LCMain.get().getServer().getScheduler().cancelTask(taskID);
        }

        if (inventory.equals(player.getOpenInventory().getTopInventory())) {
            player.closeInventory();
        }

        state = State.CLOSED;
    }

    private void fill() {
        for (int i = 0; i < size; i++)
            inventory.setItem(i, data.unSelectedItemStack(player, crate));
    }

    public Player getPlayer() {
        return player;
    }

    // Get the LootCollection visual signifier panel (not the reward item)
    private ItemStack getPanel(int slot) {
        return this.lootChances[slot].itemStack(player);
    }

    public void onInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);

        // If inventory clicked outside, nothing matters
        Inventory clickedInventory = e.getClickedInventory();
        if (clickedInventory == null)
            return;

        if (e.getClick() != ClickType.DOUBLE_CLICK
                && !e.isShiftClick()
                && e.isLeftClick()) {

            int slot = e.getSlot();

            // If crate GUI clicked on
            if (inventory.equals(clickedInventory)) {
                switch (state) {
                    case SELECTING:
                        selectSlot(slot);
                        break;
                    case REVEALING:
                        //do nothing
                        break;
                    case REVEALED: {

                        // If slot is selected
                        TmpSlot tmpSlot = slots.get(slot);

                        // If slot does not exist
                        // If there was an item on mouse virtual slot
                        if (tmpSlot == null
                                || getPlayer().getItemOnCursor().getType() != Material.AIR)
                            return;

                        // If the slot was flipped, do nothing else
                        if (flipSlot(slot, tmpSlot))
                            return;

                        // Give item
                        if (tmpSlot.randomLoot.execute(this)) {
                            e.setCancelled(false);
                        } else // Remove item
                            inventory.setItem(slot, null);

                        slots.remove(slot);
                        if (slots.isEmpty()) {
                            if (data.autoCloseTime == 0) {
                                // close inventory
                                //close();
                                // TODO fix
                                close();
                            } else if (data.autoCloseTime > 0) {
                                // TODO fix
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        close();
                                    }
                                }.runTaskLater(LCMain.get(), data.autoCloseTime);
                            }
                        }

                        break;
                    }
                }
            } else { //if (clickedInventory == e.getWhoClicked().getInventory()) {
                // TODO give control back over hotbar
                //if (Objects.equals(hostItem, e.getClickedInventory().getItem(slot))) {
                //
                //}
                if (hostItem == null || !hostItem.equals(clickedInventory.getItem(slot))) {
                    e.setCancelled(false);
                } else {
                    // TODO fix this, better message? play a sound instead?
                    player.sendMessage(ChatColor.RED + "Try clicking somewhere else");
                    //player.playSound(player.getLocation(), Sound);
                    player.playNote(player.getLocation(), Instrument.BASS_GUITAR, Note.natural(0, Note.Tone.C));
                }
            }
        }
    }

    public enum State {
        SELECTING,
        REVEALING,
        REVEALED,
        CLOSING, // TODO is this required for anything?
        CLOSED
    }
}