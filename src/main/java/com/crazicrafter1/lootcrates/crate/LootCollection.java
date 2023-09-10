package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.*;
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
import java.util.*;

public class LootCollection {
    public final String id;
    public ItemStack itemStack;
    //public List<ILoot> loot;
    public WeightedRandomContainer<ILoot> loot;

    public LootCollection copy() {
        final String strippedId = LCMain.NUMBER_AT_END.matcher(id).replaceAll("");
        String newId;
        //noinspection StatementWithEmptyBody
        for (int i = 0; LCMain.get().rewardSettings.lootSets.containsKey(newId = strippedId + i); i++) {}

        return new LootCollection(newId, itemStack.clone(),
                new WeightedRandomContainer<>(loot.getMap().entrySet().stream().collect(CollectorUtils.toLinkedMap(e -> e.getKey().copy(), Map.Entry::getValue))));
    }

    // <= rev 6
    public LootCollection(String id, ItemStack item, List<ILoot> loot) {
        // pass this through to the weighted map constructor
        // a default equal weight of 1 is given to each item
        this(id, item, new WeightedRandomContainer<>(loot.stream().collect(CollectorUtils.toLinkedMap(k -> k, v -> 1))));

        //this(id, item, new WeightedRandomContainer<>(
          //      loot.stream().collect(LinkedHashMap::new, (map, v) -> map.put(v, 1), Map::putAll)));
    }

    // > rev 7 (item weights)
    public LootCollection(String id, ItemStack itemStack, WeightedRandomContainer<ILoot> loot) {
        this.id = id;
        this.itemStack = itemStack;

        this.loot = new WeightedRandomContainer<>(
                loot.getMap().entrySet().stream().collect(//LinkedHashMap::new, (map, v) -> map.put(v, )
                CollectorUtils.toLinkedMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

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
        return String.format("%.02f%%", 100.f * ((float) loot.get(item)/(float)loot.getTotalWeight()));
    }

    private String getFormattedFraction(ILoot item) {
        return String.format("%d/%d", loot.get(item), loot.getTotalWeight());
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
                  String.format(Lang.EDITOR_ID, id) + "\n"
                + String.format(Lang.EDITOR_LOOT_LORE, loot.getMap().size()) + "\n"
                + Lang.EDITOR_LMB_EDIT + "\n"
                + Lang.EDITOR_COPY + "\n"
                + Lang.EDITOR_DELETE
        ).build();
    }

    public AbstractMenu.Builder getBuilder() {
        return new ListMenu.LBuilder()
                .title(p -> id)
                .parentButton(4, 5)
                /*
                 * Add all Loot Items
                 */
                .addAll((self1, p00) -> {
                    ArrayList<Button> result1 = new ArrayList<>();
                    for (Map.Entry<ILoot, Integer> entry : loot.getMap().entrySet()) {
                        final ILoot a = entry.getKey();

                        ItemStack copy = a.getMenuIcon();

                        AbstractMenu.Builder menu = a.getMenuBuilder().title(p -> a.getClass().getSimpleName());

                        result1.add(new Button.Builder()
                                .icon(p -> ItemBuilder.copy(copy)
                                        .lore(a.getMenuDesc() + "\n"
                                                + "&7Weight: " + getFormattedFraction(a) + " (" + getFormattedPercent(a) + ") - NUM\n" + Lang.EDITOR_LMB_EDIT + "\n" + Lang.EDITOR_DELETE + "\n" + Lang.EDITOR_COUNT_BINDS + "\n" + Lang.EDITOR_COUNT_CHANGE).build())

                                .child(self1, menu)
                                .rmb(interact -> {
                                    if (interact.shift && loot.getMap().size() > 1) {
                                        // delete
                                        loot.remove(a);
                                        return Result.refresh();
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
                                    return Result.refresh();
                                })
                                .get());
                    }
                    return result1;
                })
                .childButton(3, 5, p -> ItemBuilder.copy(itemStack).name(Lang.EDITOR_EDIT_ICON).lore(Lang.EDITOR_LMB_EDIT).build(), new ItemModifyMenu()
                        .build(this.itemStack, itemStack -> this.itemStack = itemStack))
                .childButton(5, 5, p -> ItemBuilder.copy(Material.GOLDEN_CARROT).name(Lang.NEW_LOOT).lore(Lang.EDITOR_LMB_ADD).build(), new ListMenu.LBuilder()
                        .title(p -> Lang.EDITOR_LOOT_NEW_TITLE)
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

                                            return Result.open(menu);
                                        })
                                        .get());
                            }
                            return result1;
                        })
                )
                .button(7, 5, new Button.Builder().icon(p -> ItemBuilder.copy(Material.PAPER).name(Lang.LOOT_HELP_TITLE).lore(Lang.LOOT_HELP_SUB).build()))
                .capture(new Button.Builder().lmb(e -> {
                    if (e.heldItem != null) {
                        addLoot(new LootItem(e.heldItem), 1);
                        return Result.refresh();
                    }
                    return Result.ok();
                }));
    }

}
