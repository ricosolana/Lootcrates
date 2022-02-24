package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ColorMode;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.*;
import com.willfp.ecoitems.items.EcoItem;
import com.willfp.ecoitems.items.EcoItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class LootEcoItem extends AbstractLootItem {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.BEACON).name("&cAdd LootEcoItem...").build();

    public String id;

    public LootEcoItem() {
        id = EcoItems.values().get(0).getId();
    }

    public LootEcoItem(Map<String, Object> args) {
        super(args);

        id = (String) args.get("id");
    }

    @Override
    public ItemStack getIcon(Player p) {
        return Objects.requireNonNull(EcoItems.getByID(id)).getItemStack();
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ParallaxMenu.PBuilder()
                .parentButton(4, 5)
                //.childButton(2, 5, () -> new ItemBuilder(Material.COMPASS).name("&eSearch..."), new )
                .button(3, 5, new Button.Builder().icon(p -> getIcon(null)))
                .childButton(5, 5, p -> ItemBuilder.copyOf(Material.COMPASS).name("&eBy id...").build(), new TextMenu.TBuilder()
                        .title(p -> "Assign by name")
                        .leftRaw(p -> id, null, ColorMode.STRIP)
                        .right(p -> "&eSet item by id")
                        .onClose((player) -> Result.PARENT())
                        .onComplete((player, s, b) -> {
                            EcoItem ecoItem = EcoItems.getByID(s);
                            if (ecoItem != null) {
                                this.id = s;
                                return Result.PARENT();
                            }
                            return Result.TEXT("Invalid");
                        })
                )
                .addAll((self, p0) -> {
                    ArrayList<Button> result = new ArrayList<>();

                    EcoItems.values().forEach(ecoItem -> {
                        if (ecoItem.getId().equals(this.id)) {
                            result.add(new Button.Builder()
                                    .icon(p -> ItemBuilder.copyOf(getIcon(null)).build())
                                    .get());
                        } else {
                            result.add(new Button.Builder()
                                    .icon(p -> ecoItem.getItemStack())
                                    .lmb(interact -> {
                                        // change
                                        id = ecoItem.getId();
                                        return Result.PARENT();
                                    }).get()
                            );
                        }
                    });

                    return result;
                });
    }
}
