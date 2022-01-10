package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ListenerOnPlayerInteract extends BaseListener {

    public ListenerOnPlayerInteract(Main plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        plugin.debug("Player interact caught");
        Player p = e.getPlayer();

        if (!p.hasPermission("lootcrates.open"))
            return;

        if (!Main.get().openCrates.containsKey(p.getUniqueId())) {
            plugin.debug("Passed unopened crate validation");

            Action a = e.getAction();
            if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {

                plugin.debug("Passed click validation");

                ItemStack item = p.getInventory().getItemInHand(); //.getItemInMainHand();

                Crate crate = LootCratesAPI.extractCrateFromItem(item);
                if (crate != null) {
                    plugin.debug("Successful crate validation");



                    plugin.debug(p.getDisplayName() + " has just opened a " + crate.id + " crate");
                    LootCratesAPI.openCrate(p, crate.id, p.getInventory().getHeldItemSlot());

                    e.setCancelled(true);

                }

            }

        }
    }


}
