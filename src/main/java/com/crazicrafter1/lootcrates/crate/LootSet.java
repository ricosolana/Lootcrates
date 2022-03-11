package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class LootSet implements ConfigurationSerializable {

    public String id;
    public ItemBuilder item;
    public ArrayList<ILoot> loot;

    public LootSet(String id, ItemStack item, ArrayList<ILoot> loot) {
        this.id = id;
        this.item = ItemBuilder.mutable(item);
        this.loot = loot;
    }

    public LootSet(Map<String, Object> args) {
        int rev = Main.get().rev;
        if (rev < 2) {
            item = ItemBuilder.mutable((ItemStack) args.get("itemStack"));
        } else if (rev == 2) {
            item = ((ItemBuilder) args.get("item"));
        }
        loot = (ArrayList<ILoot>) args.get("loot");
    }

    public ItemStack itemStack(@Nonnull Player p) {
        return item.copy()
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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("item", item);
        result.put("loot", loot);

        return result;
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
                .childButton(3, 5, p -> item.copy().name(Lang.EDIT_ICON).lore(Lang.LMB_EDIT).build(), new ItemModifyMenu()
                        .build(this.item.build(), itemStack -> (this.item = ItemBuilder.mutable(itemStack)).build()))
                .childButton(5, 5, p -> ItemBuilder.copyOf(Material.NETHER_STAR).name(Lang.LMB_NEW).build(), new ParallaxMenu.PBuilder()
                        .title(p -> Lang.ADD_LOOT)
                        .parentButton(4, 5)
                        .addAll((self1, p00) -> {
                            ArrayList<Button> result1 = new ArrayList<>();
                            for (Map.Entry<Class<? extends ILoot>, ItemStack> entry
                                    : LootCratesAPI.lootClasses.entrySet()) {
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
