package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.ui.AbstractMenu;
import com.crazicrafter1.crutils.ui.Button;
import com.crazicrafter1.crutils.ui.ParallaxMenu;
import com.crazicrafter1.crutils.ui.Result;
import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import com.crazicrafter1.lootcrates.crate.loot.LootNBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LootSetSettings {
    public final String id;
    public ItemStack item;
    public List<ILoot> loot;

    // fallback
    public LootSetSettings(String id, ItemStack item, List<ILoot> loot) {
        this.id = id;
        this.item = item;
        this.loot = loot.stream().map(v -> v instanceof LootNBTItem ? new LootItem(((LootNBTItem) v).item) : v).collect(Collectors.toList());
    }

    //public LootSetSettings(String id, Map<String, Object> args) {
    //    this.id = id;
    //    this.item = ((ItemBuilder) args.get("item"));
    //    this.loot = (ArrayList<ILoot>) args.get("loot");
    //}

    public void serialize(ConfigurationSection section) {
        section.set("item", item);
        section.set("loot", loot);
    }

    public ItemStack itemStack(@Nonnull Player p) {
        return ItemBuilder.copy(item)
                .placeholders(p)
                .renderAll()
                .build();
    }

    public ILoot getRandomLoot() {
        return loot.get((int) (Math.random() * loot.size()));
    }

    /**
     * Add the specified loot, and return it
     * @param iLoot loot
     * @return the loot instance
     */
    public ILoot addLoot(ILoot iLoot) {
        loot.add(iLoot);
        return iLoot;
    }

    public AbstractMenu.Builder getBuilder() {
        return new ParallaxMenu.PBuilder()
                .title(p -> id)
                .parentButton(4, 5)
                .addAll((self1, p00) -> {
                    ArrayList<Button> result1 = new ArrayList<>();
                    for (ILoot a : loot) {
                        ItemStack copy = a.getMenuIcon();

                        AbstractMenu.Builder menu = a.getMenuBuilder().title(p -> a.getClass().getSimpleName());

                        result1.add(new Button.Builder()
                                .icon(p -> ItemBuilder.copyOf(copy).lore(a.getMenuDesc() + "\n" + Lang.LMB_EDIT + "\n" + Lang.RMB_DELETE).build())

                                .child(self1, menu, interact -> {
                                    if (loot.size() > 1) {
                                        // delete
                                        loot.remove(a);
                                        return Result.REFRESH();
                                    }
                                    return null;
                                })
                                .get());
                    }
                    return result1;
                })
                .childButton(3, 5, p -> ItemBuilder.copy(item).name(Lang.EDIT_ICON).lore(Lang.LMB_EDIT).build(), new ItemModifyMenu()
                        .build(this.item, itemStack -> this.item = itemStack))
                .childButton(5, 5, p -> ItemBuilder.copyOf(Material.NETHER_STAR).name(Lang.LMB_NEW).build(), new ParallaxMenu.PBuilder()
                        .title(p -> Lang.ADD_LOOT)
                        .parentButton(4, 5)
                        .addAll((self1, p00) -> {
                            ArrayList<Button> result1 = new ArrayList<>();
                            for (Map.Entry<Class<? extends ILoot>, ItemStack> entry
                                    : LootCratesAPI.lootClasses.entrySet()) {
                                // todo remove post
                                if (entry.getKey().equals(LootNBTItem.class))
                                    continue;

                                //AbstractLoot aLootInstance = new a
                                result1.add(new Button.Builder()
                                        // This causes a nullptr because it is instantly constructed?
                                        //.icon(() -> ItemBuilder.copyOf(Material.GOLD_INGOT).name(menuClazz.getSimpleName()).build())
                                        //.child(self1.parentMenuBuilder, lootSet.addLoot(
                                        //        (ILoot) ReflectionUtil.invokeConstructor(entry.getKey())).getMenuBuilder())

                                        .icon(p -> entry.getValue())

                                        .lmb(interact -> {
                                            AbstractMenu.Builder menu = this.addLoot(
                                                    (ILoot) ReflectionUtil.invokeConstructor(entry.getKey())).getMenuBuilder();

                                            menu.parent(self1.parentMenuBuilder)
                                                    .title(p -> menu.getClass().getSimpleName());

                                            return Result.OPEN(menu);
                                        })
                                        .get());
                            }
                            return result1;
                        })
                );
    }

}
