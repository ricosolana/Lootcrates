package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.*;
import me.zombie_striker.customitemmanager.CustomBaseObject;
import me.zombie_striker.qg.api.QualityArmory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
                .title("LootItemQA")
                .parentButton(4, 5)
                //.childButton(2, 5, () -> new ItemBuilder(Material.COMPASS).name("&eSearch..."), new )
                .button(3, 5, new Button.Builder().icon(this::getIcon))
                .childButton(5, 5, () -> new ItemBuilder(Material.COMPASS).name("&eBy name...").toItem(), new TextMenu.TBuilder()
                        .title("&8Assign by name")
                        .text(this.name)
                        .rightInput(new Button.Builder().icon(() -> new ItemBuilder(Material.IRON_SWORD).name("&eSet item by name").toItem()))
                        .onClose(player -> EnumResult.BACK)
                        .onComplete((player, s) -> {
                            CustomBaseObject customBaseObject = QualityArmory.getCustomItemByName(s);
                            if (customBaseObject != null) {
                                this.name = s;
                                return EnumResult.BACK;
                            }
                            player.sendMessage(ChatColor.RED + "Must be an existing Quality Armory item");
                            return EnumResult.OK;
                        })
                )
                .action(self -> {
                    ArrayList<Button> result = new ArrayList<>();

                    QualityArmory.getCustomItems().forEachRemaining(customBaseObject -> {
                        if (customBaseObject.getName().equals(this.name)) {
                            result.add(new Button.Builder()
                                    .icon(() -> new ItemBuilder(getIcon()).glow(true).toItem())
                                    .get());
                        } else {
                            result.add(new Button.Builder()
                                    .icon(() -> QualityArmory.getCustomItemAsItemStack(customBaseObject.getName()))
                                    //.icon(() -> new ItemBuilder(customBaseObject.getItemData().getMat())
                                    //.name(customBaseObject.getName()).lore(customBaseObject.getCustomLore()).toItem())
                                    .lmb(interact -> {
                                        // change
                                        name = customBaseObject.getName();
                                        return EnumResult.BACK;
                                    }).get()
                            );
                        }
                    });

                    return result;
                });
    }
}
