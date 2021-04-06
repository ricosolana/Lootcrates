package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.util.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ListenerOnPlayerInteract extends BaseListener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        plugin.debug("Player interact caught");
        Player p = e.getPlayer();

        if (!p.hasPermission("crates.open"))
            return;

        if (!Main.openCrates.containsKey(p.getUniqueId()))
        {
            plugin.debug("Passed unopened crate validation");

            Action a = e.getAction();
            if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {

                plugin.debug("Passed click validation");

                ItemStack item = p.getInventory().getItemInHand();

                Crate crate = Crate.crateByItem(item);
                if (crate != null) {
                    plugin.debug("Successful crate validation");

                    plugin.debug(p.getDisplayName() + " has just opened a " + crate.getId() + " crate");
                    Crate.openCrate(p, crate.getId(), p.getInventory().getHeldItemSlot());

                    e.setCancelled(true);

                }

            }

        }
    }


}
