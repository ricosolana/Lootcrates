package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.Main;
import me.zombie_striker.customitemmanager.CustomBaseObject;
import me.zombie_striker.qg.api.QualityArmory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;

public class LootItemQA extends AbstractLootItem {

    public String name;

    /**
     * Default ctor
     */
    public LootItemQA() {
        // just the first loaded item
        name = QualityArmory.getCustomItems().next().getName();
    }

    public LootItemQA(Map<String, Object> args) {
        super(args);
        this.name = (String)args.get("name");
    }

    public LootItemQA(String name, int min, int max) {
        super(min, max);
        this.name = name;
    }

    @Override
    public ItemStack getIcon(Player p) {
        return ItemBuilder.copyOf(QualityArmory.getCustomItemAsItemStack(name)).placeholders(p).build();
    }

    @Override
    public String toString() {
        return "&8Quality armory: &f" + name + "\n" +
                super.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("name", name);

        return super.serialize();
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ParallaxMenu.PBuilder()
                .title("LootItemQA", true)
                .parentButton(4, 5)
                //.childButton(2, 5, () -> new ItemBuilder(Material.COMPASS).name("&eSearch..."), new )
                .button(3, 5, new Button.Builder().icon(() -> getIcon(null)))
                .childButton(5, 5, () -> ItemBuilder.copyOf(Material.COMPASS).name("&eBy name...").build(), new TextMenu.TBuilder()
                        .title("&8Assign by name")
                        .leftRaw(() -> name)
                        .right(() -> "&eSet item by name")
                        .onClose((player, reroute) -> {
                            Main.get().info("LootItemQA reroute: " + reroute);
                            return !reroute ? Result.BACK() : null;
                        })
                        .onComplete((player, s) -> {
                            CustomBaseObject customBaseObject = QualityArmory.getCustomItemByName(s);
                            if (customBaseObject != null) {
                                this.name = s;
                                return Result.BACK();
                            }
                            return Result.TEXT("Invalid");
                        })
                )
                .addAll(self -> {
                    ArrayList<Button> result = new ArrayList<>();

                    QualityArmory.getCustomItems().forEachRemaining(customBaseObject -> {
                        if (customBaseObject.getName().equals(this.name)) {
                            result.add(new Button.Builder()
                                    .icon(() -> ItemBuilder.copyOf(getIcon(null)).glow(true).build())
                                    .get());
                        } else {
                            result.add(new Button.Builder()
                                    .icon(() -> QualityArmory.getCustomItemAsItemStack(customBaseObject.getName()))
                                    //.icon(() -> new ItemBuilder(customBaseObject.getItemData().getMat())
                                    //.name(customBaseObject.getName()).lore(customBaseObject.getCustomLore()).toItem())
                                    .lmb(interact -> {
                                        // change
                                        name = customBaseObject.getName();
                                        return Result.BACK();
                                    }).get()
                            );
                        }
                    });

                    return result;
                });
    }
}
