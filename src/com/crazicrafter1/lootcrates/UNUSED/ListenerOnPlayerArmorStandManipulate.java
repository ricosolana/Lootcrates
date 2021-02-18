package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Config;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.NMSHandler;
import net.minecraft.server.v1_14_R1.Blocks;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class ListenerOnPlayerArmorStandManipulate extends BaseListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorstandInteract(PlayerArmorStandManipulateEvent e) {

        //e.getRightClicked().setCustomName();
        if (e.getRightClicked().getCustomName() != null && e.getRightClicked().getCustomName().equals("crateRuinsArmorStand")) {
            if (e.getPlayerItem().getType() == Material.AIR) {
                plugin.debug("ArmorStandManipulate : armorstand would have been removed (not removed due to debug)), hopefully giving item");
                //e.getRightClicked().remove();

                if (Config.ruinGen && Main.getInstance().getConfig().getBoolean("remove-ruins")) {
                    World w = e.getRightClicked().getLocation().getWorld();
                    int x = e.getRightClicked().getLocation().getBlockX();
                    int y = e.getRightClicked().getLocation().getBlockY();
                    int z = e.getRightClicked().getLocation().getBlockZ();

                    //Block[] blocks = new Block[] {Blocks.COBBLESTONE, Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.OBSIDIAN};
                    Material[] materials = new Material[]{Material.COBBLESTONE, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.OBSIDIAN};

                    int r = 4;

                    for (int rx = -r; rx < r; rx++) {

                        for (int rz = -r; rz < r; rz++) {

                            for (int ry = -r - 1; ry < r + 1; ry++) {

                                int h = y + ry;

                                switch (w.getBlockAt(x + rx, h, z + rz).getType()) {

                                    case CAULDRON:
                                        NMSHandler.setBlock(Blocks.AIR, w, x + rx, h, z + rz);
                                        break;
                                    case COBBLESTONE:
                                        NMSHandler.setBlock(Blocks.GRASS_BLOCK, w, x + rx, h, z + rz);
                                        break;
                                    case STONE_BRICKS:
                                        NMSHandler.setBlock(Blocks.GRASS_BLOCK, w, x + rx, h, z + rz);
                                        break;
                                    case IRON_BARS:
                                        NMSHandler.setBlock(Blocks.AIR, w, x + rx, h, z + rz);
                                        break;
                                    case OBSIDIAN:
                                        NMSHandler.setBlock(Blocks.GRASS_BLOCK, w, x + rx, h, z + rz);
                                        break;
                                    case MOSSY_STONE_BRICKS:
                                        NMSHandler.setBlock(Blocks.GRASS_BLOCK, w, x + rx, h, z + rz);
                                        break;

                                }

                            }
                        }
                    }
                }


            } else {
                plugin.debug("ArmorStandInteract Cancelled (due to player not clicking with open hand)");
                e.setCancelled(true);
            }


        }

    }

}
