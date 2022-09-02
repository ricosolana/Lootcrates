package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.crutils.Version;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.Lootcrates;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ListenerOnPlayerInteract extends BaseListener {

    public ListenerOnPlayerInteract(LCMain plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (plugin.rev == -1)
            return;

        Action a = e.getAction();

        // PHYSICAL describes stepping onto pressure plate or tripwire...
        if (a == Action.PHYSICAL)
            return;

        if (e.useItemInHand() == Event.Result.DENY)
            return;

        Player p = e.getPlayer();

        //noinspection deprecation
        ItemStack item = p.getInventory().getItemInHand();
        if (item.getType() == Material.AIR && Version.AT_LEAST_v1_9.a())
            item = p.getInventory().getItemInOffHand();

        CrateSettings crate = Lootcrates.getCrate(item);
        if (crate != null)
            e.setCancelled(true);

        if (crate == null)
            return;

        if (!CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            if (a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR) {
                if (p.hasPermission(LCMain.PERM_OPEN)) {
                    new CrateInstance(p, crate, p.getInventory().getHeldItemSlot()).open();
                } else
                    p.sendMessage(ColorUtil.renderAll(Lang.ERR_NO_PERM_OPEN));
            } else {
                if (p.hasPermission(LCMain.PERM_PREVIEW))
                    Lootcrates.displayCratePreview(p, crate);
                else
                    p.sendMessage(ColorUtil.renderAll(Lang.ERR_NO_PERM_PREVIEW));
            }
        } else
            plugin.notifier.globalWarn(Lang.Misc_OpenBug);
    }
}
