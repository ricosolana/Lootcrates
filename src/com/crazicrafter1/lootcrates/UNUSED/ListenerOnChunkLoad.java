package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ListenerOnChunkLoad extends BaseListener {

    private final float CHANCE = .02f;

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {

        if (Config.worldGen && Config.crateSpawnWorldChances.containsKey(e.getWorld().getName()) && e.isNewChunk() && e.getChunk().isLoaded()) {

            float worldChance = ((float)Config.crateSpawnWorldChances.get(e.getWorld().getName()))/100.0f;

            //Main.debug("world chance : " + worldChance);

            if (Util.randomChance(worldChance)) {

                int chunkX = e.getChunk().getX();
                int chunkZ = e.getChunk().getZ();
                // then spawn a "item" that is a crate

                int x = chunkX*16 + Util.randomRange(0, 15);
                int z = chunkZ*16 + Util.randomRange(0, 15);

                Location loc = new Location(e.getWorld(), x, e.getWorld().getHighestBlockYAt(x, z), z, 0, 0);

                //if (e.getWorld().getBlockAt(loc).getType() != Material.GRASS_BLOCK) return;

                Location safeLoc = getSafe(loc);
                if (safeLoc == null) return;
                Crate.spawnCrateRuins(safeLoc);

                plugin.debug("Spawned a crate at (" + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + ")");

            }

        }

    }

    private Location getSafe(Location location) {
//org.bukkit
        World w = location.getWorld();

        int signX = 1;
        int signZ = 1;

        for (int rx = 0; rx<40; rx++) {

            for (int rz = 0; rz<40; rz++) {

                int x = location.getBlockX()+(rx/2)*signX;
                int z = location.getBlockZ()+(rz/2)*signZ;

                int h = w.getHighestBlockYAt(x, z)-1;

                for (int y = h; y>h-4; y--) {
                    if (w.getBlockAt(x, y, z).getType() == Material.GRASS_BLOCK) return new Location(w, x, y, z);
                }

                signX*=-1;
            }

            signZ*=-1;
        }

        return null;

    }

}
