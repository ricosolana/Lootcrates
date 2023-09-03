package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ui.*;
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

    public static final ItemStack EDITOR_ICON = ItemBuilder.copy(Material.CROSSBOW).name("&8Add QualityArmory item...").build();

    public String name;

    /**
     * Editor template LootItemQA ctor
     */
    public LootItemQA() {
        // just the first loaded item
        name = QualityArmory.getCustomItems().next().getName();
    }

    protected LootItemQA(LootItemQA other) {
        super(other);
        this.name = other.name;
    }

    public LootItemQA(Map<String, Object> args) {
        super(args);
        this.name = (String) args.get("name");
    }

    @NotNull
    @Override
    public ItemStack getRenderIcon(@NotNull Player p) {
        return ofRange(p, QualityArmory.getCustomItemAsItemStack(name));
    }

    @NotNull
    @Override
    public ItemStack getMenuIcon() {
        return QualityArmory.getCustomItemAsItemStack(name);
    }

    @NotNull
    @Override
    public String getMenuDesc() {
        return "&8Quality armory: &f" + name + "\n" +
                super.getMenuDesc();
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
        return rangeButtons(new ListMenu.LBuilder()
                .parentButton(4, 5)
                //.childButton(2, 5, () -> new ItemBuilder(Material.COMPASS).name("&eSearch..."), new )
                .button(3, 5, new Button.Builder().icon(p -> getMenuIcon()))
                .childButton(5, 5, p -> ItemBuilder.copy(Material.COMPASS).name(Lang.ASSIGN_EXACT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.ASSIGN_EXACT)
                        .leftRaw(p -> name)
                        .right(p -> Lang.SET_BY_NAME)
                        .onClose((player) -> Result.parent())
                        .onComplete((p, s, b) -> {
                            CustomBaseObject customBaseObject = QualityArmory.getCustomItemByName(s);
                            if (customBaseObject != null) {
                                this.name = s;
                                return Result.parent();
                            }
                            return Result.text(Lang.ERR_INVALID);
                        })
                )
                .addAll((self, p00) -> {
                    ArrayList<Button> result = new ArrayList<>();

                    QualityArmory.getCustomItems().forEachRemaining(customBaseObject -> {
                        if (customBaseObject.getName().equals(this.name)) {
                            result.add(new Button.Builder()
                                    .icon(p -> getMenuIcon())
                                    .get());
                        } else {
                            result.add(new Button.Builder()
                                    .icon((p) -> QualityArmory.getCustomItemAsItemStack(customBaseObject.getName()))
                                    .lmb(interact -> {
                                        // change
                                        name = customBaseObject.getName();
                                        return Result.parent();
                                    }).get()
                            );
                        }
                    });

                    return result;
                }), getMenuIcon(), 1, 5, 2, 5);
    }

    @NotNull
    @Override
    public LootItemQA copy() {
        return new LootItemQA();
    }
}
