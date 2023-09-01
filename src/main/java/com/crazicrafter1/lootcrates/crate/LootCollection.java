package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.MathUtil;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.WeightedRandomContainer;
import com.crazicrafter1.crutils.ui.AbstractMenu;
import com.crazicrafter1.crutils.ui.Button;
import com.crazicrafter1.crutils.ui.ListMenu;
import com.crazicrafter1.crutils.ui.Result;
import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LootCollection {
    public final String id;
    public ItemStack itemStack;
    //public List<ILoot> loot;
    public WeightedRandomContainer<ILoot> loot;

    public LootCollection copy() {
        final String strippedId = LCMain.NUMBER_AT_END.matcher(id).replaceAll("");
        String newId;
        for (int i = 0; LCMain.get().rewardSettings.lootSets.containsKey(newId = strippedId + i); i++) {}

        //loot.getMap().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().copy(), Map.Entry::getValue));

        return new LootCollection(newId, itemStack.clone(),
                new WeightedRandomContainer<>(loot.getMap().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().copy(), Map.Entry::getValue))));
    }

    // <= rev 6
    public LootCollection(String id, ItemStack item, List<ILoot> loot) {
        // pass this through to the weighted map constructor
        // a default equal weight of 1 is given to each item
        this(id, item, new WeightedRandomContainer<>(loot.stream().collect(Collectors.toMap(k -> k, v -> 1))));
    }

    // > rev 7 (item weights)
    public LootCollection(String id, ItemStack itemStack, WeightedRandomContainer<ILoot> loot) {
        this.id = id;
        this.itemStack = itemStack;
        //this.loot = loot.stream().map(v -> v instanceof LootNBTItem ? new LootItem(((LootNBTItem) v).itemStack) : v).collect(Collectors.toList());

        //loot.getMap().entrySet().stream().collect(Collectors.toMap(e1 -> e1.getKey() instanceof LootNBTItem ? new LootItem(((LootNBTItem) e1)) : e1, e2 -> e2.getValue()))

        //loot.getMap().entrySet().stream().collect(
        //        Collectors.toMap(e1 -> e1.getKey().copy(), e2 -> e2.getValue()));

        // condense any old LootNBTItems to LootItem, forward it otherwise
        this.loot = new WeightedRandomContainer<>(loot.getMap().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    //public LootSetSettings(String id, Map<String, Object> args) {
    //    this.id = id;
    //    this.item = ((ItemBuilder) args.get("item"));
    //    this.loot = (ArrayList<ILoot>) args.get("loot");
    //}

    public void serialize(ConfigurationSection section) {
        section.set("item", itemStack);
        //section.set("loot", loot.getMap());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<ILoot, Integer> entry : loot.getMap().entrySet()) {
            //section.set("loot");
            Map<String, Object> map = new HashMap<>();
            map.put("loot", entry.getKey());
            map.put("weight", entry.getValue());
            result.add(map);
        }
        section.set("loot", result);
    }

    public ItemStack itemStack(@Nonnull Player p) {
        return ItemBuilder.copy(itemStack)
                .placeholders(p)
                .renderAll()
                .build();
    }

    public ILoot getRandomLoot() {
        return loot.getRandom();
    }

    /**
     * Add the specified loot, and return it
     * @param iLoot loot
     * @return the loot instance
     */
    private ILoot addLoot(ILoot iLoot, int weight) {
        loot.add(iLoot, weight);
        return iLoot;
    }

    private String getFormattedPercent(ILoot item) {
        return String.format("%.02f%%", 100.f * ((float) loot.get(item)/(float)loot.getWeight()));
    }

    private String getFormattedFraction(ILoot item) {
        return String.format("%d/%d", loot.get(item), loot.getWeight());
    }

    public ItemStack getMenuIcon() {
        //return ItemBuilder.copy(itemStack)
        //        .replace("lootset_id", id, '%')
        //        .replace("lootset_size", "" + loot.getMap().size(), '%')
        //        .lore(
        //                  String.format(Lang.FORMAT_ID, id) + "\n"
        //                + String.format(Lang.ED_LootSets_BTN_LORE, loot.getMap().size()) + "\n"
        //                + Lang.ED_LMB_EDIT + "\n"
        //                + Lang.ED_RMB_COPY + "\n"
        //                + Lang.ED_RMB_SHIFT_DELETE
        //).build();

        return ItemBuilder.copy(itemStack).lore(
                  String.format(Lang.FORMAT_ID, id) + "\n"
                + String.format(Lang.ED_LootSets_BTN_LORE, loot.getMap().size()) + "\n"
                + Lang.ED_LMB_EDIT + "\n"
                + Lang.ED_RMB_COPY + "\n"
                + Lang.ED_RMB_SHIFT_DELETE
        ).build();
    }

    public AbstractMenu.Builder getBuilder() {
        return new ListMenu.LBuilder()
                .title(p -> id)
                .parentButton(4, 5)
                .addAll((self1, p00) -> {
                    ArrayList<Button> result1 = new ArrayList<>();
                    for (Map.Entry<ILoot, Integer> entry : loot.getMap().entrySet()) {
                        final ILoot a = entry.getKey();

                        ItemStack copy = a.getMenuIcon();

                        AbstractMenu.Builder menu = a.getMenuBuilder().title(p -> a.getClass().getSimpleName());

                        result1.add(new Button.Builder()
                                .icon(p -> ItemBuilder.copy(copy)
                                        .lore(a.getMenuDesc() + "\n"
                                                + "&7Weight: " + getFormattedFraction(a) + " (" + getFormattedPercent(a) + ") - NUM\n" + Lang.ED_LMB_EDIT + "\n" + Lang.ED_RMB_SHIFT_DELETE + "\n" + Lang.ED_NUM_SUM + "\n" + Lang.ED_NUM_SUM_DESC).build())

                                .child(self1, menu)
                                .rmb(interact -> {
                                    if (interact.shift && loot.getMap().size() > 1) {
                                        // delete
                                        loot.remove(a);
                                        return Result.REFRESH();
                                    }
                                    return null;
                                })
                                .num(interact -> {
                                    // weight modifiers
                                    final int n = interact.numberKeySlot;
                                    int change = n == 0 ? -5 : n == 1 ? -1 : n == 2 ? 1 : n == 3 ? 5 : 0;

                                    if (change != 0) {
                                        // then change weight
                                        loot.add(a, MathUtil.clamp(loot.get(a) + change, 1, Integer.MAX_VALUE));
                                    }
                                    return Result.REFRESH();
                                })
                                .get());
                    }
                    return result1;
                })
                .childButton(3, 5, p -> ItemBuilder.copy(itemStack).name(Lang.EDIT_ICON).lore(Lang.ED_LMB_EDIT).build(), new ItemModifyMenu()
                        .build(this.itemStack, itemStack -> this.itemStack = itemStack))
                .childButton(5, 5, p -> ItemBuilder.copy(Material.GOLDEN_CARROT).name(Lang.LMB_NEW).build(), new ListMenu.LBuilder()
                        .title(p -> Lang.ED_LootSets_PROTO_New_TI)
                        .parentButton(4, 5)
                        .addAll((self1, p00) -> {
                            ArrayList<Button> result1 = new ArrayList<>();
                            for (Map.Entry<Class<? extends ILoot>, ItemStack> entry
                                    : LCMain.get().lootClasses.entrySet()) {

                                //AbstractLoot aLootInstance = new a
                                result1.add(new Button.Builder()
                                        // This causes a nullptr because it is instantly constructed?
                                        //.icon(() -> ItemBuilder.copyOf(Material.GOLD_INGOT).name(menuClazz.getSimpleName()).build())
                                        //.child(self1.parentMenuBuilder, lootSet.addLoot(
                                        //        (ILoot) ReflectionUtil.invokeConstructor(entry.getKey())).getMenuBuilder())

                                        .icon(p -> entry.getValue())

                                        .lmb(interact -> {
                                            AbstractMenu.Builder menu = this.addLoot(
                                                    (ILoot) ReflectionUtil.invokeConstructor(entry.getKey()), 1).getMenuBuilder();

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
