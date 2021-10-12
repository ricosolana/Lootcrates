package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.EnumResult;
import com.crazicrafter1.gapi.ParallaxMenu;
import me.zombie_striker.qg.api.QualityArmory;
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
    public ItemStack getIcon() {
        return QualityArmory.getCustomItemAsItemStack(name);
    }

    @Override
    public String toString() {
        return "&8Quality armory: " + name + "\n" +
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
                .title("LootItemQA")
                .parentButton(4, 5)
                .action(self -> {
                    ArrayList<Button> result = new ArrayList<>();

                    QualityArmory.getCustomItems().forEachRemaining(customBaseObject -> {
                        result.add(new Button.Builder()
                                .icon(() -> new ItemBuilder(customBaseObject.getItemData().getMat())
                                        .name(customBaseObject.getName()).lore(customBaseObject.getCustomLore()).toItem())
                                .lmb(interact -> {
                                    // change
                                    name = customBaseObject.getName();
                                    return EnumResult.BACK;
                                }).get()
                        );
                    });

                    return result;
                });
    }
}
