package com.crazicrafter1.lootcrates;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerLog {

    private static final File PATH = new File(LCMain.get().getDataFolder(), "players");

    //public boolean antiExploit = false;
    private static final HashMap<UUID, PlayerLog> LOGS = new HashMap<>();
    // get crate open information and other stuff to analyze possible duping or whatever
    //
    private final HashMap<String, List<String>> crates = new HashMap<>();

    public void increment(String id) {
        List<String> dateList = crates.computeIfAbsent(id, k -> new ArrayList<>());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        dateList.add(formatter.format(date));
    }


    @Nonnull
    public static PlayerLog get(UUID uuid) {
        // if not present add

        PlayerLog stat = PlayerLog.LOGS.get(uuid);
        if (stat == null) {
            stat = new PlayerLog();
            PlayerLog.LOGS.put(uuid, stat);
        }

        return stat;
    }

    public static void saveAll(@Nonnull CommandSender sender) {
        try {
            for (Map.Entry<UUID, PlayerLog> entry : PlayerLog.LOGS.entrySet()) {
                String rawUUID = entry.getKey().toString();
                YamlConfiguration playerConfig = new YamlConfiguration();
                for (Map.Entry<String, List<String>> entry1 : entry.getValue().crates.entrySet()) {
                    playerConfig.set(entry1.getKey(), entry1.getValue());
                }
                playerConfig.save(new File(PATH, rawUUID + ".yml"));
            }
        } catch (Exception e) {
            LCMain.get().notifier.severe(sender, String.format(Lang.CONFIG_ERROR10, e.getMessage()));
        }
    }

    public static void loadAll(@Nonnull CommandSender sender) {
        try {
            if (LCMain.get().rev < 5)
                return;

            if (!PATH.exists() || !PATH.isDirectory())
                return;

            // playerStatsPath.mkdirs();

            File[] files = PATH.listFiles(); // TODO use Paths.walk

            for (File file : files) {
                try {
                    String rawUUID = file.getName().replace(".yml", "");
                    UUID uuid = UUID.fromString(rawUUID);

                    YamlConfiguration playerConfig = new YamlConfiguration();
                    playerConfig.load(file);

                    PlayerLog stat = new PlayerLog();
                    PlayerLog.LOGS.put(uuid, stat);
                    for (String id : playerConfig.getKeys(false))
                        stat.crates.put(id, playerConfig.getStringList(id));

                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            LCMain.get().notifier.severe(sender, String.format(Lang.CONFIG_ERROR11, e.getMessage()));
        }
    }

}
