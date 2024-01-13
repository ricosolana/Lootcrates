package com.crazicrafter1.lootcrates.listeners

import com.crazicrafter1.crutils.ColorUtil
import com.crazicrafter1.lootcrates.*
import com.crazicrafter1.lootcrates.crate.CrateInstance
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent

class ListenerCrates(plugin: LCMain?) : BaseListener(plugin) {
    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onCrateClick(e: PlayerInteractEvent) {
        val action = e.action

        // PHYSICAL describes stepping onto pressure plate or tripwire...
        if (action == Action.PHYSICAL) return
        if (e.useItemInHand() == Event.Result.DENY) return
        val p = e.player
        val inventory = p.inventory
        var item = inventory.itemInHand
        if (item.type.isAir) item = inventory.itemInOffHand
        val crate = Lootcrates.getCrate(item) ?: return
        e.setCancelled(true)
        if (!CrateInstance.Companion.CRATES.containsKey(p.uniqueId)) {
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                if (!BaseListener.Companion.plugin!!.checkCerts || Lootcrates.canClaimTicket(item) != null) {
                    if (p.hasPermission(LCMain.Companion.PERM_OPEN)) {
                        CrateInstance(p, crate, item).open()
                    } else p.sendMessage(ColorUtil.renderAll(Lang.CRATE_ERROR_OPEN))
                } else {
                    // TODO improve logging
                    BaseListener.Companion.plugin!!.notifier!!.warn("A crate appears to be duplicated! " + p.name)
                }
            } else {
                if (p.hasPermission(LCMain.Companion.PERM_PREVIEW)) Lootcrates.showPreview(p, crate) else p.sendMessage(ColorUtil.renderAll(Lang.CRATE_ERROR_PREVIEW))
            }
        } else BaseListener.Companion.plugin!!.notifier!!.globalWarn(Lang.Misc_OpenBug)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onCrateMenuAction(e: InventoryClickEvent) {
        val p = e.whoClicked as Player

        // If player is opening crate
        val instance: CrateInstance = CrateInstance.Companion.CRATES.get(p.uniqueId)
        if (instance != null) {
            instance.onInventoryClick(e)
        }
        //else if (e.getClick().isCreativeAction()
        //    && Lootcrates.getCrate(e.getCurrentItem()) != null) {
        //    // cancel
        //    e.setCancelled(true);
        //}


        /* else if (e.getClickedInventory() != null) {
            if (LootCratesAPI.extractCrateFromItem(e.getCursor()) != null) {

                Main.get().info(e.getClickedInventory().getType().name());

                switch (e.getClickedInventory().getType()) {
                    case ANVIL:
                    case SMOKER:
                    case BREWING:
                    case FURNACE:
                    case CRAFTING:
                    case MERCHANT:
                    case WORKBENCH:
                    case ENCHANTING:
                    case GRINDSTONE:
                    case STONECUTTER:
                    case BLAST_FURNACE:
                        e.setCancelled(true);
                        break;
                }
            }
        }*/
    }

    @EventHandler
    private fun onPlayerQuit(e: PlayerQuitEvent) {
        val p = e.player
        if (CrateInstance.Companion.CRATES.containsKey(p.uniqueId)) {
            Lootcrates.stopCrate(p)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun onFireworkDamage(e: EntityDamageByEntityEvent) {
        if (CrateInstance.Companion.crateFireworks.contains(e.damager)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    private fun onCrateClose(e: InventoryCloseEvent) {
        val p = e.player as Player
        if (CrateInstance.Companion.CRATES.containsKey(p.uniqueId)) {
            Lootcrates.stopCrate(p)
        }
    }
}
