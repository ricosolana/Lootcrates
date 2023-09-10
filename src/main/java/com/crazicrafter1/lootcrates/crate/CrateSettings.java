package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.crutils.ui.*;
import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.LCMain;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class CrateSettings {
    public enum RevealType {
        GOOD_OL_DESTY,
        WASD,
        CSGO,
        POPCORN,
    }

    public final String id;
    public String title;
    public int columns;
    public int picks;
    public Sound sound;
    public RevealType revealType;

    // TODO deleting the lootset in editor will cause an error when crate is opened
    // because the string referencing to key in map will be removed
    // should use WeakReference
    //private WeightedRandomContainer<String> loot;
    public WeightedRandomContainer<String> loot;
    public ItemStack item;

    public CrateSettings copy() {
        final String strippedId = LCMain.NUMBER_AT_END.matcher(id).replaceAll("");
        String newId;
        //noinspection StatementWithEmptyBody
        for (int i = 0; LCMain.get().rewardSettings.crates.containsKey(newId = strippedId + i); i++);

        return new CrateSettings(newId, title, columns, picks, sound, new HashMap<>(loot.getMap()), Lootcrates.tagItemAsCrate(item.clone(), newId), revealType);
    }

    //todo hmmm kinda ugly
    //@Deprecated
    //public CrateSettings(String id) {
    //    this.id = id;
    //    this.title = "select loot";
    //    this.columns = 3;
    //    this.picks = 4;
    //    this.sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    //    this.loot = new WeightedRandomContainer<>();
    //    this.item = ItemBuilder.mut(LootcratesAPI.getCrateAsItem(new ItemStack(Material.ENDER_CHEST), id)).name("my new crate").build();
    //}

    //todo remove post-migrate
    public CrateSettings(String id, String title, int columns, int picks, Sound sound, Map<String, Integer> loot, ItemStack item, RevealType revealType) {
        this.id = id;
        this.title = title;
        this.columns = columns;
        this.picks = picks;
        this.sound = sound;
        this.loot = new WeightedRandomContainer<>(loot);
        this.item = item;
        this.revealType = revealType;
    }

    //public CrateSettings(String id, Map<String, Object> args) {
    //    this.id = id;
    //    this.title = ColorUtil.renderMarkers((String) args.get("title"));
    //    this.columns = (int) args.get("columns");
    //    this.picks = (int) args.get("picks");
    //    this.sound = Sound.valueOf((String) args.get("sound"));
    //    this.loot = new WeightedRandomContainer<>((Map<LootSetSettings, Integer>) args.get("weights"));
    //    this.item = (ItemStack) args.get("item");
    //}

    @NotNull
    public void serialize(ConfigurationSection section) {
        section.set("item", item);
        section.set("title", ColorUtil.invertRendered(title));
        section.set("columns", columns);
        section.set("picks", picks);
        section.set("sound", sound.name());
        section.set("weights", loot.getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        section.set("revealType", revealType.name());
    }

    private String getFormattedPercent(LootCollection lootGroup) {
        return String.format("%.02f%%", 100.f * ((float) loot.get(lootGroup.id)/(float) loot.getTotalWeight()));
    }

    private String getFormattedFraction(LootCollection lootGroup) {
        return String.format("%d/%d", loot.get(lootGroup.id), loot.getTotalWeight());
    }

    public LootCollection getRandomLootSet() {
        return LCMain.get().rewardSettings.lootSets.get(loot.getRandom());
    }

    /**
     * Remove the lootset by id if present
     * If the last lootset was removed from this crate then a blank
     * @param
     */
    //public void removeLootSet(String id) {
    //    loot.remove(id);
    //    if (loot.getMap().isEmpty()) {
    //        LootSetSettings lootSetSettings = Main.get().rewardSettings.lootSets.values().iterator().next();
    //        loot.add(lootSetSettings.id, 1);
    //    }
    //}

    public ItemStack getMenuIcon() {
        return ItemBuilder.copy(item).renderAll().lore(
                  String.format(Lang.EDITOR_ID, id) + "\n"
                + Lang.EDITOR_LMB_EDIT + "\n"
                + Lang.EDITOR_COPY + "\n"
                + Lang.EDITOR_DELETE).build();
    }

    /**
     * Return the macro formatted item, or unformatted if player is null
     * @param p player
     * @return the formatted item
     */
    public ItemStack itemStack(@Nullable Player p) {
        ItemStack itemStack = ItemBuilder.copy(item)
                .replace("crate_picks", "" + picks, '%')
                .placeholders(p)
                .renderAll()
                .build();

        if (LCMain.get().checkCerts) {
            ReadWriteNBT nbt = NBT.itemStackToNBT(itemStack);
            ReadWriteNBT tag = nbt.getCompound("tag");
            UUID ticket = UUID.randomUUID();
            tag.setUUID("CrateCert", ticket);

            LCMain.crateCerts.add(ticket);

            return NBT.itemStackFromNBT(nbt);
        }

        return itemStack;
    }

    public String getTitle(@Nullable Player p) {
        return ColorUtil.renderAll(Util
                .placeholders(p, this.title
                        .replace("%crate_picks%", "" + picks)
                ));
    }

    @Override
    public String toString() {
        return "id: " + id + "\n" +
                "itemStack: " + item + "\n" +
                "title: " + title + "\n" +
                "size: " + title + "\n" +
                "picks: " + picks + "\n" +
                "sound: " + sound + "\n" +
                "weights: " + loot + "\n" +
                "revealType" + revealType.name() + "\n";
    }

    public AbstractMenu.Builder getBuilder() {
        return new SimpleMenu.SBuilder(5)
                .title(p -> id)
                .background()
                //.onOpen(p -> p.setGameMode(GameMode.CREATIVE))
                //.onClose(p -> {
                //    p.setGameMode();
                //    return Result.PARENT();
                //})
                .parentButton(4, 4)
                // *   *   *
                // Edit Crate ItemStack
                // *   *   *
                .childButton(1, 1, p -> ItemBuilder.copy(item).name(Lang.EDITOR_EDIT_ITEM).lore(Lang.EDITOR_LMB_EDIT).build(), new ItemModifyMenu()
                        .build(item, itemStack ->
                                ItemBuilder.mut(item)
                                        .apply(itemStack,
                                                ItemBuilder.FLAG_NAME | ItemBuilder.FLAG_LORE | ItemBuilder.FLAG_SKULL | ItemBuilder.FLAG_MATERIAL)
                                        .build())
                )
                // Edit Inventory Title
                .childButton(3, 1, p -> ItemBuilder.copy(Material.PAPER).name(String.format(Lang.EDITOR_EDIT_TITLE, title)).lore(Lang.EDITOR_LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.TITLE)
                        .leftRaw(p -> title)
                        .onClose((player) -> Result.parent())
                        .right(p -> Lang.EDITOR_FORMATTING, p -> Editor.getColorDem(), ColorUtil.AS_IS)
                        .onComplete((p, s, b) -> {
                            if (!s.isEmpty()) {
                                title = ColorUtil.RENDER_MARKERS.a(s);
                                return Result.parent();
                            }

                            return Result.text(Lang.COMMAND_ERROR_INPUT);
                        })
                )
                // *   *   *
                // Edit LootSets
                // *   *   *
                .childButton(5, 1, p -> ItemBuilder.from("EXPERIENCE_BOTTLE").name(Lang.EDITOR_LOOT1).lore(Lang.EDITOR_LMB_EDIT).build(), new ListMenu.LBuilder()
                        .title(p -> Lang.EDITOR_LOOT1)
                        .parentButton(4, 5)
                        .onClose((player) -> Result.parent())
                        .addAll((builder, p) -> {
                            ArrayList<Button> result1 = new ArrayList<>();

                            for (LootCollection lootSet : LCMain.get().rewardSettings.lootSets.values()) {
                                Integer weight = loot.get(lootSet.id);
                                Button.Builder btn = new Button.Builder();
                                ItemBuilder b = ItemBuilder.copy(lootSet.itemStack.getType()).name("&8" + lootSet.id);

                                if (weight != null) {
                                    b.lore( "&7Weight: " + getFormattedFraction(lootSet) + " (" + getFormattedPercent(lootSet) + ") - NUM\n" +
                                            Lang.EDITOR_LMB_TOGGLE + "\n" +
                                            Lang.EDITOR_COUNT_BINDS + "\n" +
                                            Lang.EDITOR_COUNT_CHANGE).glow(true);

                                    btn.lmb(interact -> {
                                        if (loot.getMap().size() > 1) {
                                            // toggle inclusion
                                            loot.remove(lootSet.id);
                                            return Result.refresh();
                                        }
                                        return null;
                                    }).num(interact -> {
                                        // weight modifiers
                                        final int n = interact.numberKeySlot;
                                        int change = n == 0 ? -5 : n == 1 ? -1 : n == 2 ? 1 : n == 3 ? 5 : 0;

                                        if (change != 0) {
                                            // then change weight
                                            loot.add(lootSet.id, MathUtil.clamp(weight + change, 1, Integer.MAX_VALUE));
                                        }
                                        return Result.refresh();
                                    });
                                } else {
                                    b.lore(Lang.EDITOR_LMB_TOGGLE);
                                    btn.lmb(interact -> {
                                        loot.add(lootSet.id, 1);
                                        return Result.refresh();
                                    });
                                }
                                result1.add(btn.icon(p1 -> b.build()).get());
                            }

                            return result1;
                        })
                )
                // *   *   *
                // Edit Columns
                // *   *   *
                .button(7, 1, new Button.Builder()
                        .icon(p -> ItemBuilder.copy(Material.LADDER).name(String.format(Lang.EDITOR_CRATE_COLUMNS, columns)).lore(Lang.EDITOR_LMB_DECREMENT + "\n" + Lang.EDITOR_INCREMENT).amount(columns).build())
                        .lmb(interact -> {
                            // decrease
                            columns = MathUtil.clamp(columns - 1, 1, 6);
                            return Result.refresh();
                        })
                        .rmb(interact -> {
                            // decrease
                            columns = MathUtil.clamp(columns + 1, 1, 6);
                            return Result.refresh();
                        }))
                // *   *   *
                // Edit Picks
                // *   *   *
                .button(2, 3, new Button.Builder()
                        .icon(p -> ItemBuilder.copy(Material.MELON_SEEDS).name(String.format(Lang.EDITOR_CRATE_PICKS, picks)).lore(Lang.EDITOR_LMB_DECREMENT + "\n" + Lang.EDITOR_INCREMENT).amount(picks).build())
                        .lmb(interact -> {
                            // decrease
                            picks = MathUtil.clamp(picks - 1, 1, columns*9);
                            return Result.refresh();
                        })
                        .rmb(interact -> {
                            // decrease
                            picks = MathUtil.clamp(picks + 1, 1, columns*9);
                            return Result.refresh();
                        }))
                // *   *   *
                // Edit Pick Sound
                // *   *   *
                .childButton(6, 3, p -> ItemBuilder.copy(Material.JUKEBOX).name(String.format(Lang.EDITOR_CRATE_SOUND, sound)).lore(Lang.EDITOR_LMB_EDIT).build(),
                        new TextMenu.TBuilder()
                                .title(p -> Lang.EDITOR_CRATE_SOUND_TITLE)
                                .leftRaw(p -> Editor.LOREM_IPSUM)
                                .right(p -> Lang.EDITOR_CRATE_SOUND_INPUT)
                                .onClose((player) -> Result.parent())
                                .onComplete((p, s, b) -> {
                                    try {
                                        sound = Sound.valueOf(s.toUpperCase());
                                        p.playSound(p.getLocation(), sound, 1, 1);
                                        return Result.parent();
                                    } catch (Exception e) {
                                        return Result.text(Lang.COMMAND_ERROR_INPUT);
                                    }
                                })
                );
    }

    public AbstractMenu.Builder getPreview() {
        return new ListMenu.LBuilder()
                .title(p -> "Preview")
                .addAll((self, p) -> {
                    List<Button> buttons = new ArrayList<>();

                    for (Map.Entry<String, Integer> entry : loot.getMap().entrySet()) {
                        LootCollection lootSet = LCMain.get().rewardSettings.lootSets.get(entry.getKey());
                        int weight = entry.getValue();
                        double chance = ((double)weight / (double)loot.getTotalWeight()) * 100.0;
                        buttons.add(new Button.Builder()
                                .icon(p00 -> ItemBuilder.copy(lootSet.itemStack).lore(String.format("&8%.02f%%", chance)).build()).get());
                    }

                    return buttons;
                });
    }

}

