package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ColorMode;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.Lang;
import me.zombie_striker.customitemmanager.CustomBaseObject;
import me.zombie_striker.qg.api.QualityArmory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;

import static com.crazicrafter1.lootcrates.Lang.L;

public class LootItemQA extends AbstractLootItem {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.CROSSBOW).name("&8Add QualityArmory item...").build();

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
        this.name = (String) args.get("name");
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

        return result;
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ParallaxMenu.PBuilder()
                .parentButton(4, 5)
                //.childButton(2, 5, () -> new ItemBuilder(Material.COMPASS).name("&eSearch..."), new )
                .button(3, 5, new Button.Builder().icon((p) -> getIcon(null)))
                .childButton(5, 5, p -> ItemBuilder.copyOf(Material.COMPASS).name("&e" + L(p, Lang.A.Assign_by_name)).build(), new TextMenu.TBuilder()
                        .title(p -> L(p, Lang.A.Assign_by_name))
                        .leftRaw(p -> name, null, ColorMode.STRIP)
                        .right(p -> "&e" + L(p, Lang.A.Set_item_by_name))
                        .onClose((player) -> Result.PARENT())
                        .onComplete((p, s, b) -> {
                            CustomBaseObject customBaseObject = QualityArmory.getCustomItemByName(s);
                            if (customBaseObject != null) {
                                this.name = s;
                                return Result.PARENT();
                            }
                            return Result.TEXT(L(p, Lang.A.Invalid));
                        })
                )
                .addAll(self -> {
                    ArrayList<Button> result = new ArrayList<>();

                    QualityArmory.getCustomItems().forEachRemaining(customBaseObject -> {
                        if (customBaseObject.getName().equals(this.name)) {
                            result.add(new Button.Builder()
                                    .icon((p) -> ItemBuilder.copyOf(getIcon(null)).build())
                                    .get());
                        } else {
                            result.add(new Button.Builder()
                                    .icon((p) -> QualityArmory.getCustomItemAsItemStack(customBaseObject.getName()))
                                    .lmb(interact -> {
                                        // change
                                        name = customBaseObject.getName();
                                        return Result.PARENT();
                                    }).get()
                            );
                        }
                    });

                    return result;
                });
    }
}
