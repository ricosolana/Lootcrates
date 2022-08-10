package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.crutils.Version;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
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

    public ListenerOnPlayerInteract(Main plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (plugin.rev == -1)
            return;

        Action a = e.getAction();
        //if (!(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK))
//            return;

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

        CrateSettings crate = LootCratesAPI.extractCrateFromItem(item);
        if (crate != null)
            e.setCancelled(true);

        if (crate == null)
            return;

        if (!plugin.openCrates.containsKey(p.getUniqueId())) {
            if (p.hasPermission("lootcrates.open") && (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK))
                LootCratesAPI.openCrate(p, crate.id, p.getInventory().getHeldItemSlot());
            else if (p.hasPermission("lootcrates.preview") && (a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK))
                LootCratesAPI.previewCrate(p, crate.id);
        } else {
            plugin.errorAdmin("Either a player is exploitative or LootCrates has bugged out: " + p.getName());
            plugin.errorAdmin("If you think this is expected behaviour, contact dev");
        }
    }


}
