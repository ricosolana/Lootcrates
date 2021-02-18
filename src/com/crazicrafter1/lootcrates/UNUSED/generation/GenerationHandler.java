package com.crazicrafter1.lootcrates.generation;

import com.crazicrafter1.lootcrates.LootcratesAPI;
import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class GenerationHandler {

    private Main plugin;

    GenerationHandler(Main plugin) {
        this.plugin = plugin;

        //int rand =
        int end = 0;
        for (String crateType : plugin.config.crates.keySet()) { //.crateTypeWorldChances.keySet()) {
            for (int i=0; i<plugin.config.crateTypeWorldChances.get(crateType); i++) {
                randMap[end] = crateType;
                end++;
            }
        }
    }

    private String[] randMap = new String[100];

    private ItemStack randomCrate() {
        //Bukkit.getPluginManager().getPlugin("")
        return LootcratesAPI.getConfig().crates.get(randMap[Util.randomRange(0,99)]).getPreppedItemStack(true, 1);
        //return Config.crate_items.get(randMap[Util.randomRange(0,99)]);
    }

    public void spawnCrateRuins(Location loc) {

        //Location loc = new Location(e.getWorld(), x, e.getWorld().getHighestBlockYAt(x, z), z, 0, 0);
        World w = loc.getWorld();

        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int y = loc.getWorld().getHighestBlockYAt(x, z);

        //ArmorStand as = w.spawn(new Location(loc.getWorld(), x+1.15, y-.65, z+.25), ArmorStand.class);
        //ArmorStand as = w.spawn(new Location(loc.getWorld(), x+1.15, y-.35, z+.25), ArmorStand.class);
        ArmorStand as = w.spawn(new Location(loc.getWorld(), x+.5, y-1.4, z+.5), ArmorStand.class);

        //e.getWorld().dropItemNaturally(loc, randomCrate());

        as.setArms(true);
        as.setBasePlate(false);
        as.setVisible(false);
        as.setInvulnerable(true);
        //as.setItemInHand(randomCrate());
        as.setHelmet(randomCrate());
        //as.setChestplate(randomCrate());
        as.setRightArmPose(new EulerAngle(-Math.PI/12.0, Math.PI/4.0, 0));
        as.setGravity(false);
        as.setAI(false);
        as.setCollidable(false);
        as.setCustomName("crateRuinsArmorStand");





        if (plugin.config.areCratesRuined()) {

            NMSHandler.setBlock(Blocks.CAULDRON, w, x, y, z);
            //Block[] blocks = new Block[] {Blocks.COBBLESTONE, Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.OBSIDIAN};
            Material[] materials = new Material[]{Material.COBBLESTONE, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.OBSIDIAN};

            int r = 4;

            for (int rx = -r; rx < r; rx++) {

                for (int rz = -r; rz < r; rz++) {

                    for (int ry = -r; ry < r; ry++) {

                        int h = y + ry;

                        if (!(w.getBlockAt(x + rx, h, z + rz).getType() == Material.GRASS_BLOCK ||
                                w.getBlockAt(x + rx, h, z + rz).getType() == Material.SAND)) continue;

                        // fill with blocks
                        int sqDist = Util.sqDist(rx, rz, 0, 0);

                        float n = ((float) ((r * r) - sqDist + 1));
                        float d = (float) (r * r);

                        if (sqDist <= r * r) {
                            if (Util.randomChance(n / d)) {

                                //Block randBlock = blocks[(int) (Math.random() * blocks.length)];
                                //NMSHandler.setBlock(randBlock, w, x + rx, h, z + rz);


                                Material randMaterial = materials[(int) (Math.random() * materials.length)];
                                w.getBlockAt(x + rx, h, z + rz).setType(randMaterial);

                            }
                            if (Util.randomChance((((float) (r * r) - n) / d) - .3f)) {

                                //NMSHandler.setBlock(Blocks.IRON_BARS, w, x + rx, h+1, z + rz);
                                w.getBlockAt(x + rx, h + 1, z + rz).setType(Material.IRON_BARS);

                            }
                        }
                    }
                }
            }
        }

    }


}
