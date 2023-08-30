package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.Version;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.Lootcrates;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ListenerOnPlayerInteract extends BaseListener {

    public ListenerOnPlayerInteract(LCMain plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
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
        if (item.getType().isAir() && Version.AT_LEAST_v1_9.a())
            item = inventory.getItemInOffHand();

        CrateSettings crate = Lootcrates.getCrate(item);
        if (crate != null)
            e.setCancelled(true);

        if (crate == null)
            return;

        if (!CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                if (!plugin.checkCerts || Lootcrates.canClaimTicket(item) != null) {
                    if (p.hasPermission(LCMain.PERM_OPEN)) {
                        new CrateInstance(p, crate, item).open();
                    } else
                        p.sendMessage(ColorUtil.renderAll(Lang.ERR_NO_PERM_OPEN));
                } else {
                    // TODO improve logging
                    plugin.notifier.warn("A crate appears to be duplicated! " + p.getName());
                }
            } else {
                if (p.hasPermission(LCMain.PERM_PREVIEW))
                    Lootcrates.showPreview(p, crate);
                else
                    p.sendMessage(ColorUtil.renderAll(Lang.ERR_NO_PERM_PREVIEW));
            }
        } else
            plugin.notifier.globalWarn(Lang.Misc_OpenBug);
    }
}
