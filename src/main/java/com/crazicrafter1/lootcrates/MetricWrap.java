package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.Metrics;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricWrap {

    public static void init(Main plugin) {
        try {
            Metrics metrics = new Metrics(plugin, 10395);

            metrics.addCustomChart(new Metrics.SimplePie("update",
                    () -> "" + plugin.update));

            metrics.addCustomChart(new Metrics.SimplePie("language",
                    () -> plugin.language));

            metrics.addCustomChart(
                new Metrics.AdvancedPie("loot",
                        () -> plugin.data.lootSets.keySet().stream().map(
                                lootSet -> new AbstractMap.SimpleEntry<>(lootSet, 1)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            );

            metrics.addCustomChart(
                    new Metrics.AdvancedPie("crates",
                            () -> plugin.data.crates.keySet().stream().map(
                                    crate -> new AbstractMap.SimpleEntry<>(crate, 1)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            );

        } catch (Exception e) {
            plugin.error("Unable to enable bStats Metrics (" + e.getMessage() + ")");
        }
    }

}
