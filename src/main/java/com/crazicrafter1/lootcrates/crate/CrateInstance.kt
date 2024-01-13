package com.crazicrafter1.lootcrates.crate

import com.crazicrafter1.crutils.*
import com.crazicrafter1.lootcrates.LCMain
import com.crazicrafter1.lootcrates.Lootcrates
import com.crazicrafter1.lootcrates.PlayerLog
import com.crazicrafter1.lootcrates.RewardSettings
import com.crazicrafter1.lootcrates.crate.CrateSettings.RevealType
import com.crazicrafter1.lootcrates.crate.loot.ILoot
import org.apache.commons.lang3.Validate
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream

class CrateInstance(p: Player?, crate: CrateSettings?, hostItem: ItemStack?) {
    private class TmpSlot internal constructor(var randomLoot: ILoot?) {
        var isHidden = true
    }

    // Constants
    val player: Player
    private val crate: CrateSettings?
    private val size: Int
    private val picks: Int
    private val sound: Sound?
    private val lootChances: Array<LootCollection?>
    private val inventory: Inventory

    // Live variables
    private val slots = HashMap<Int, TmpSlot>()
    private var state = State.SELECTING
    private var taskID = -1

    //private int lockSlot;
    private val hostItem: ItemStack?
    private val data: RewardSettings? = LCMain.Companion.get()!!.rewardSettings

    init {
        player = p!!
        this.crate = crate
        size = crate!!.columns * 9
        picks = crate.picks
        sound = crate.sound
        lootChances = arrayOfNulls(size)
        inventory = Bukkit.createInventory(p, size, ColorUtil.renderAll(crate.getTitle(p)))
        this.hostItem = hostItem
        populate(crate)
        this.fill()
    }

    fun open() {
        CRATES[player.uniqueId] = this
        player.openInventory(inventory)
    }

    private fun populate(crate: CrateSettings?) {
        for (i in lootChances.indices) {
            lootChances[i] = crate.getRandomLootSet()
        }
    }

    private fun selectSlot(slot: Int) {
        if (slots.containsKey(slot)) return
        inventory.setItem(slot, data!!.selectedItemStack(player, crate))
        val randomLoot = lootChances[slot].getRandomLoot()
        slots[slot] = TmpSlot(randomLoot)
        if (sound != null) player.playSound(player.location, sound, 1f, 1f)

        // Play animatic on final pick
        if (slots.size == picks) {
            // "Cost" of opening crate
            if (hostItem != null) {
                Validate.isTrue(!LCMain.Companion.get()!!.checkCerts || Lootcrates.claimTicket(hostItem) != null)
                hostItem.amount = hostItem.amount - 1
            }
            state = State.REVEALING
            if (data.speed != 0) {
                when (crate!!.revealType) {
                    RevealType.GOOD_OL_DESTY -> startDestyAnimation()
                    RevealType.WASD -> startWASDAnimation()
                    RevealType.POPCORN -> startPopcornAnimation()
                    else -> pop()
                }
            } else {
                pop()
            }
        }
    }

    private fun startDestyAnimation() {
        taskID = object : BukkitRunnable() {
            var iterations = 0
            override fun run() {
                // Panel reveal
                if (iterations < size) {
                    inventory.setItem(iterations, getPanel(iterations))
                } else if (iterations > size + 10 / data!!.speed) {
                    cancel()
                    pop()
                }
                iterations++
            }
        }.runTaskTimer(LCMain.Companion.get()!!, 20, data!!.speed.toLong()).taskId
    }

    // This is not the actual csgo animation because more than 1 pick can be picked
    // instead an animation of vertically/horizontally translating tiles in each row/col
    // will be played
    private fun startWASDAnimation() {
        taskID = object : BukkitRunnable() {
            val maxIterations = size + 10 / data!!.speed
            var iterations = 0
            var x0 = 0
            var y0 = 0
            override fun run() {

                // TODO
                //  maybe have rows with more selected icons to scroll faster than those with less

                // sequenced horizontal and vertical scrolling
                if (iterations < maxIterations / 4) {
                    x0++
                } else if (iterations < maxIterations / 2) {
                    y0++
                } else if (iterations >= maxIterations - maxIterations / 4) {
                    y0--
                } else {
                    x0--
                }

                //x0 %= 9;
                //y0 %= size / 9;

                // Set the panels from the left to right and shift each time
                for (x in 0..8) {
                    for (y in 0 until size / 9) {
                        // horizontal scrolling
                        //inventory.setItem(x + y * 9,
                        //        getPanel(((x + iterations) % 9) + y + 9)
                        //);
                        var ix = (x0 + x) % 9
                        var iy = (y0 + y) % (size / 9)
                        if (ix < 0) ix += 9
                        if (iy < 0) iy += size / 9

                        //ItemBuilder.mut().gl
                        inventory.setItem(x + y * 9,
                                getPanel(ix + iy * 9)
                        )
                    }
                }

                // timer expiry check
                if (iterations > maxIterations) {
                    cancel()
                    pop()
                }
                iterations++
            }
        }.runTaskTimer(LCMain.Companion.get()!!, 20, data!!.speed.toLong()).taskId
    }

    private fun startPopcornAnimation() {
        taskID = object : BukkitRunnable() {
            val maxIterations = size + 10 / data!!.speed
            var iterations = 0

            //int delay = 10;
            var delay = 5.0

            //int timer = 0;
            private var availableSlots: List<Int?>? = null

            init {
                availableSlots = IntStream.range(0, size).boxed().filter { i: Int -> !slots.containsKey(i) }.collect(Collectors.toList())
                Collections.shuffle(availableSlots)
            }

            override fun run() {
                val r = Math.random()
                val t = 1.0 / (1 + delay)
                if (delay <= 0 || r < t) {
                    delay -= r
                    player.spawnParticle(if (r > t / 3.0) Particle.SMOKE_LARGE else Particle.SMOKE_NORMAL,
                            player.location, (20.0 / (5.0 + delay)).toInt())
                    if (iterations < availableSlots!!.size) {
                        inventory.setItem(availableSlots!![iterations]!!,
                                if (r < t / 3.0) ItemStack(Material.AIR) else ItemBuilder.copy(if (r > t * 2.0 / 3.0) Material.POLISHED_BLACKSTONE_BUTTON else Material.DARK_OAK_BUTTON).name("&8Burnt").build())
                        if (r > t / 3.0) player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, (.9 + r / 20.0).toFloat()) else player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_SNARE, 1f, (.8 + r / 20.0).toFloat())
                    } else if (iterations > maxIterations) {
                        cancel()
                        pop()
                    }
                    iterations++
                }
            }
        }.runTaskTimer(LCMain.Companion.get()!!, 20, 1).taskId
    }

    private fun pop() {
        for (i in 0 until size) {
            if (!slots.containsKey(i)) {
                inventory.setItem(i, null)
            } else inventory.setItem(i, getPanel(i))
        }
        if (data!!.fireworkEffect != null) explosion()
        state = State.REVEALED
    }

    private fun explosion() {
        val loc = player.location

        // TODO because the firework is purely visual
        //  make client side only
        // how? maybe protocol lib
        //  im not sure whether fireworks are server side or what
        val firework = loc.getWorld()!!.spawnEntity(loc, EntityType.FIREWORK) as Firework

        // adding
        crateFireworks.add(firework)
        val fwm = firework.fireworkMeta
        fwm.addEffects(data!!.fireworkEffect)
        firework.fireworkMeta = fwm
        firework.detonate()
    }

    /**
     * A panel was clicked, show it
     */
    private fun flipSlot(slot: Int, tmpSlot: TmpSlot): Boolean {
        if (!tmpSlot.isHidden) {
            return false
        }
        val visual = tmpSlot.randomLoot!!.getRenderIcon(player)
        inventory.setItem(slot, visual)
        tmpSlot.isHidden = false
        return true
    }

    fun close() {
        // Give items if still making selecting
        if (state == State.CLOSED) {
            return
        }
        if (state != State.SELECTING) {
            for ((key, value) in slots) {
                if (value.randomLoot!!.execute(this)) {
                    if (value.isHidden) Util.give(player, value.randomLoot!!.getRenderIcon(player)) else Util.give(player, inventory.getItem(key))
                }
            }
        }
        PlayerLog.Companion.get(player.uniqueId).increment(crate!!.id)
        if (state == State.REVEALING) {
            LCMain.Companion.get()!!.getServer().getScheduler().cancelTask(taskID)
        }
        if (inventory == player.openInventory.topInventory) {
            player.closeInventory()
        }
        state = State.CLOSED
    }

    private fun fill() {
        for (i in 0 until size) inventory.setItem(i, data!!.unSelectedItemStack(player, crate))
    }

    // Get the LootCollection visual signifier panel (not the reward item)
    private fun getPanel(slot: Int): ItemStack? {
        return lootChances[slot]!!.itemStack(player)
    }

    fun onInventoryClick(e: InventoryClickEvent) {
        e.isCancelled = true

        // If inventory clicked outside, nothing matters
        val clickedInventory = e.clickedInventory ?: return
        if (e.click != ClickType.DOUBLE_CLICK && !e.isShiftClick
                && e.isLeftClick) {
            val slot = e.slot

            // If crate GUI clicked on
            if (inventory == clickedInventory) {
                when (state) {
                    State.SELECTING -> selectSlot(slot)
                    State.REVEALING -> {}
                    State.REVEALED -> {


                        // If slot is selected
                        val tmpSlot = slots[slot]

                        // If slot does not exist
                        // If there was an item on mouse virtual slot
                        if (tmpSlot == null
                                || player.itemOnCursor.type != Material.AIR) return

                        // If the slot was flipped, do nothing else
                        if (flipSlot(slot, tmpSlot)) return

                        // Give item
                        if (tmpSlot.randomLoot!!.execute(this)) {
                            e.isCancelled = false
                        } else  // Remove item
                            inventory.setItem(slot, null)
                        slots.remove(slot)
                        if (slots.isEmpty()) {
                            if (data!!.autoCloseTime == 0) {
                                // close inventory
                                //close();
                                // TODO fix
                                close()
                            } else if (data.autoCloseTime > 0) {
                                // TODO fix
                                object : BukkitRunnable() {
                                    override fun run() {
                                        close()
                                    }
                                }.runTaskLater(LCMain.Companion.get()!!, data.autoCloseTime.toLong())
                            }
                        }
                    }
                }
            } else { //if (clickedInventory == e.getWhoClicked().getInventory()) {
                // TODO give control back over hotbar
                //if (Objects.equals(hostItem, e.getClickedInventory().getItem(slot))) {
                //
                //}
                if (hostItem == null || hostItem != clickedInventory.getItem(slot)) {
                    e.isCancelled = false
                } else {
                    // TODO fix this, better message? play a sound instead?
                    player.sendMessage(ChatColor.RED.toString() + "Try clicking somewhere else")
                    //player.playSound(player.getLocation(), Sound);
                    player.playNote(player.location, Instrument.BASS_GUITAR, Note.natural(0, Note.Tone.C))
                }
            }
        }
    }

    enum class State {
        SELECTING,
        REVEALING,
        REVEALED,
        CLOSING,

        // TODO is this required for anything?
        CLOSED
    }

    companion object {
        /*
     * Runtime modifiable stuff
     */
        var CRATES: MutableMap<UUID, CrateInstance> = HashMap()
        var crateFireworks = Collections.newSetFromMap(WeakHashMap<Firework, Boolean>())
    }
}