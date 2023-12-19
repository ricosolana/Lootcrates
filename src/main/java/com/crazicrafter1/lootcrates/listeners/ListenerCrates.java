package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.Lootcrates;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ListenerCrates extends BaseListener {

    public ListenerCrates(LCMain plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onCrateClick(PlayerInteractEvent e) {
        Action action = e.getAction();

        // PHYSICAL describes stepping onto pressure plate or tripwire...
        if (action == Action.PHYSICAL)
            return;

        if (e.useItemInHand() == Event.Result.DENY)
            return;

        Player p = e.getPlayer();
        PlayerInventory inventory = p.getInventory();

        //noinspection deprecation
        ItemStack item = inventory.getItemInHand();
        if (item.getType().isAir())
            item = inventory.getItemInOffHand();

        CrateSettings crate = Lootcrates.getCrate(item);
        if (crate == null)
            return;

        e.setCancelled(true);

        if (!CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                if (!plugin.checkCerts || Lootcrates.canClaimTicket(item) != null) {
                    if (p.hasPermission(LCMain.PERM_OPEN)) {
                        new CrateInstance(p, crate, item).open();
                    } else
                        p.sendMessage(ColorUtil.renderAll(Lang.CRATE_ERROR_OPEN));
                } else {
                    // TODO improve logging
                    plugin.notifier.warn("A crate appears to be duplicated! " + p.getName());
                }
            } else {
                if (p.hasPermission(LCMain.PERM_PREVIEW))
                    Lootcrates.showPreview(p, crate);
                else
                    p.sendMessage(ColorUtil.renderAll(Lang.CRATE_ERROR_PREVIEW));
            }
        } else
            plugin.notifier.globalWarn(Lang.Misc_OpenBug);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onCrateMenuAction(InventoryClickEvent e)
    {
        Player p = (Player) e.getWhoClicked();

        // If player is opening crate
        CrateInstance instance = CrateInstance.CRATES.get(p.getUniqueId());
        if (instance != null) {
            instance.onInventoryClick(e);
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
    private void onPlayerQuit(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();

        if (CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            Lootcrates.stopCrate(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onFireworkDamage(EntityDamageByEntityEvent e) {
        //noinspection SuspiciousMethodCalls
        if (CrateInstance.crateFireworks.contains(e.getDamager())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onCrateClose(InventoryCloseEvent e)
    {
        Player p = (Player)e.getPlayer();

        if (CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            Lootcrates.stopCrate(p);
        }
    }
}
