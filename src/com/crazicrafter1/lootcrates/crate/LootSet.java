package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.crazicrafter1.lootcrates.Lang.L;

public class LootSet implements ConfigurationSerializable {

    public static class Language {
        public String itemStackDisplayName;
        public String itemStackLore;
    }

    public String id;
    public ItemStack itemStack;
    public ArrayList<ILoot> loot;

    public LootSet(String id, ItemStack itemStack, ArrayList<ILoot> loot) {
        this.id = id;
        this.itemStack = itemStack;
        this.loot = loot;
    }

    public LootSet(Map<String, Object> args) {
        itemStack = (ItemStack) args.get("itemStack");
        loot = (ArrayList<ILoot>) args.get("loot");
    }

    public ItemStack itemStack(Player p) {
        Lang.Unit unit = Main.get().lang.getUnit(p);

        if (unit == null) {
            return itemStack;
        }

        Language lang = unit.lootSets.get(id);

        return ItemBuilder.copyOf(itemStack)
                .name(lang.itemStackDisplayName)
                .lore(lang.itemStackLore)
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

        result.put("itemStack", itemStack);
        result.put("loot", loot);

        return result;
    }

    @Override
    public String toString() {
        return "LootGroup{" +
                "id='" + id + '\'' +
                ", itemStack=" + itemStack.getType() +
                ", loot=" + loot +
                '}';
    }

    public AbstractMenu.Builder getBuilder() {
        return new ParallaxMenu.PBuilder()
                .title(p -> id)
                .parentButton(4, 5)
                .addAll((self1, p00) -> {
                    ArrayList<Button> result1 = new ArrayList<>();
                    for (ILoot a : loot) {
                        ItemStack copy = a.getIcon(null);

                        result1.add(new Button.Builder()
                                .icon(p -> ItemBuilder.copyOf(copy).lore(a + "\n&7" + L(Lang.A.LMB) + ": &a" + L(Lang.A.Edit) + "\n&7" + L(Lang.A.RMB) + ": &c" + L(Lang.A.Delete)).build())

                                .child(self1, a.getMenuBuilder(), interact -> {
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
                .childButton(3, 5, p -> ItemBuilder.copyOf(itemStack).name(L(Lang.A.Edit)).build(), new ItemModifyMenu()
                        .build(this.itemStack, itemStack -> this.itemStack = itemStack))
                .childButton(5, 5, p -> ItemBuilder.copyOf(Material.NETHER_STAR).name("&6" + L(Lang.A.New)).build(), new ParallaxMenu.PBuilder()
                        .title(p -> L(Lang.A.New_LootSet))
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
