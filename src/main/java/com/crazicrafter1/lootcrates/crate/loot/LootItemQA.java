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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;

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

    @NotNull
    @Override
    public ItemStack getRenderIcon(@NotNull Player p) {
        return ItemBuilder.mutable(QualityArmory.getCustomItemAsItemStack(name)).build();
    }

    @NotNull
    @Override
    public ItemStack getMenuIcon(@NotNull Player p) {
        return getRenderIcon(p);
    }

    @NotNull
    @Override
    public String getMenuDesc(@NotNull Player p) {
        return "&8Quality armory: &f" + name + "\n" +
                super.getMenuDesc(p);
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("name", name);

        return result;
    }

    @Nonnull
    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ParallaxMenu.PBuilder()
                .parentButton(4, 5)
                //.childButton(2, 5, () -> new ItemBuilder(Material.COMPASS).name("&eSearch..."), new )
                .button(3, 5, new Button.Builder().icon((p) -> getMenuIcon(null)))
                .childButton(5, 5, p -> ItemBuilder.copyOf(Material.COMPASS).name(Lang.ASSIGN_EXACT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.ASSIGN_EXACT)
                        .leftRaw(p -> name, ColorMode.STRIP_RENDERED, null, ColorMode.STRIP_RENDERED)
                        .right(p -> Lang.SET_BY_NAME)
                        .onClose((player) -> Result.PARENT())
                        .onComplete((p, s, b) -> {
                            CustomBaseObject customBaseObject = QualityArmory.getCustomItemByName(s);
                            if (customBaseObject != null) {
                                this.name = s;
                                return Result.PARENT();
                            }
                            return Result.TEXT(Lang.ERR_INVALID);
                        })
                )
                .addAll((self, p00) -> {
                    ArrayList<Button> result = new ArrayList<>();

                    QualityArmory.getCustomItems().forEachRemaining(customBaseObject -> {
                        if (customBaseObject.getName().equals(this.name)) {
                            result.add(new Button.Builder()
                                    .icon((p) -> ItemBuilder.copyOf(getRenderIcon(null)).build())
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
